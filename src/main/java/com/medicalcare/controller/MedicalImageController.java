package com.medicalcare.controller;

import com.medicalcare.domain.entity.ImageAnalysisResult;
import com.medicalcare.domain.entity.ImagingAuditLog;
import com.medicalcare.domain.entity.MedicalImage;
import com.medicalcare.domain.entity.PhysicianReview;
import com.medicalcare.security.ImagingAccessGuard;
import com.medicalcare.security.ImagingRoles;
import com.medicalcare.service.ImagingAuditService;
import com.medicalcare.service.MedicalImageService;
import com.medicalcare.service.PacsIntegrationService;
import com.medicalcare.service.PhysicianReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.medicalcare.security.DataEncryptionService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医療画像認識API（権限・監査・匿名化対応）
 */
@RestController
@RequestMapping("/api/medical-images")
@CrossOrigin(origins = "*")
public class MedicalImageController {

    private final MedicalImageService medicalImageService;
    private final PacsIntegrationService pacsIntegrationService;
    private final PhysicianReviewService physicianReviewService;
    private final ImagingAuditService imagingAuditService;
    private final ImagingAccessGuard accessGuard;
    private final DataEncryptionService dataEncryptionService;

    public MedicalImageController(MedicalImageService medicalImageService,
                                  PacsIntegrationService pacsIntegrationService,
                                  PhysicianReviewService physicianReviewService,
                                  ImagingAuditService imagingAuditService,
                                  ImagingAccessGuard accessGuard,
                                  DataEncryptionService dataEncryptionService) {
        this.medicalImageService = medicalImageService;
        this.pacsIntegrationService = pacsIntegrationService;
        this.physicianReviewService = physicianReviewService;
        this.imagingAuditService = imagingAuditService;
        this.accessGuard = accessGuard;
        this.dataEncryptionService = dataEncryptionService;
    }

    @GetMapping("/compliance/disclaimer")
    public ResponseEntity<?> disclaimer() {
        return ResponseEntity.ok(Map.of(
                "disclaimer", physicianReviewService.disclaimer(),
                "isDiagnosticClaimAllowed", false,
                "requiresPhysicianReview", true,
                "assumesFalsePositivesAndMisses", true
        ));
    }

    @GetMapping("/providers")
    public ResponseEntity<?> providers(
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            HttpServletRequest request) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW, "providers");
        return ResponseEntity.ok(medicalImageService.getProviders());
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "institutionId", required = false) Long institutionId,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = ImagingAccessGuard.HEADER_INSTITUTION_ID, required = false) Long userInstitutionId,
            HttpServletRequest request) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW, "list");
        boolean canViewPhi = ImagingRoles.allows(ImagingRoles.CAN_VIEW_PHI, role);
        List<MedicalImage> images = institutionId != null
                ? medicalImageService.listByInstitution(institutionId)
                : medicalImageService.listAll();
        List<MedicalImage> sanitized = images.stream()
                .map(img -> medicalImageService.sanitizeForRole(img, role, canViewPhi))
                .collect(Collectors.toList());
        Long userId = parseOptionalUserId(userIdHeader);
        imagingAuditService.log(userId, role, "LIST", "MEDICAL_IMAGE", null, null,
                "count=" + sanitized.size(), true, request);
        return ResponseEntity.ok(sanitized);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @PathVariable Long id,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = ImagingAccessGuard.HEADER_INSTITUTION_ID, required = false) Long userInstitutionId,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_VIEW, "get");
            MedicalImage image = medicalImageService.getById(id);
            accessGuard.requireInstitutionScope(role, userInstitutionId, image.getMedicalInstitutionId());
            boolean canViewPhi = ImagingRoles.allows(ImagingRoles.CAN_VIEW_PHI, role);
            medicalImageService.sanitizeForRole(image, role, canViewPhi);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("image", image);
            body.put("latestAnalysis", medicalImageService.latestAnalysis(id).orElse(null));
            body.put("analyses", medicalImageService.listAnalyses(id));
            body.put("reviews", physicianReviewService.listByImage(id));
            body.put("disclaimer", physicianReviewService.disclaimer());
            body.put("isDiagnosticClaimAllowed", false);

            Long userId = parseOptionalUserId(userIdHeader);
            imagingAuditService.log(userId, role, "READ", "MEDICAL_IMAGE", id, id, null, true, request);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "modality", required = false) String modality,
            @RequestParam(value = "medicalInstitutionId", required = false) Long medicalInstitutionId,
            @RequestParam(value = "applicationId", required = false) Long applicationId,
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "patientName", required = false) String patientName,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "TECHNICIAN") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_UPLOAD, "upload");
            Long uploadedByUserId = parseOptionalUserId(userIdHeader);
            MedicalImage image = medicalImageService.upload(
                    file, modality, medicalInstitutionId, applicationId,
                    uploadedByUserId, patientId, patientName);
            medicalImageService.sanitizeForRole(image, role, ImagingRoles.allows(ImagingRoles.CAN_VIEW_PHI, role));
            imagingAuditService.log(uploadedByUserId, role, "UPLOAD", "MEDICAL_IMAGE", image.getId(),
                    image.getId(), "modality=" + image.getModality() + ",encrypted=" + image.getFileEncrypted(),
                    true, request);
            return ResponseEntity.ok(Map.of(
                    "message", "画像がアップロードされました（患者情報は匿名化・暗号化して保存）",
                    "image", image,
                    "disclaimer", physicianReviewService.disclaimer()
            ));
        } catch (Exception e) {
            imagingAuditService.log(parseOptionalUserId(userIdHeader), role, "UPLOAD", "MEDICAL_IMAGE",
                    null, null, e.getMessage(), false, request);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<?> analyze(
            @PathVariable Long id,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "generateFindings", defaultValue = "true") boolean generateFindings,
            @RequestParam(value = "patientContext", required = false) String patientContext,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "TECHNICIAN") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_ANALYZE, "analyze");
            ImageAnalysisResult result = medicalImageService.analyze(
                    id, provider, generateFindings, patientContext);
            Long userId = parseOptionalUserId(userIdHeader);
            imagingAuditService.log(userId, role, "ANALYZE", "IMAGE_ANALYSIS", result.getId(), id,
                    "provider=" + result.getProvider(), true, request);
            return ResponseEntity.ok(Map.of(
                    "message", "画像解析が完了しました（診断支援候補・確定診断ではありません）",
                    "result", result,
                    "disclaimer", physicianReviewService.disclaimer(),
                    "isDiagnosticClaim", false,
                    "requiresPhysicianReview", true
            ));
        } catch (Exception e) {
            imagingAuditService.log(parseOptionalUserId(userIdHeader), role, "ANALYZE", "IMAGE_ANALYSIS",
                    null, id, e.getMessage(), false, request);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 医師による確認・承認
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> review(
            @PathVariable Long id,
            @RequestBody ReviewRequest body,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_APPROVE, "physician-review");
            Long userId = accessGuard.requireUserId(userIdHeader);
            PhysicianReview review = physicianReviewService.submitReview(
                    body.analysisResultId(),
                    userId,
                    role,
                    body.decision(),
                    body.physicianComment(),
                    body.confirmedFindingsText(),
                    body.falsePositiveNotes(),
                    body.missedFindingNotes(),
                    body.acknowledgedNonDiagnostic()
            );
            imagingAuditService.log(userId, role, "PHYSICIAN_REVIEW", "PHYSICIAN_REVIEW",
                    review.getId(), id, "decision=" + review.getDecision(), true, request);
            return ResponseEntity.ok(Map.of(
                    "message", "医師確認を記録しました（確定診断ではありません）",
                    "review", review,
                    "disclaimer", physicianReviewService.disclaimer()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/analyses")
    public ResponseEntity<?> analyses(
            @PathVariable Long id,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW, "analyses");
        return ResponseEntity.ok(medicalImageService.listAnalyses(id));
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<?> auditLogs(
            @PathVariable Long id,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW_AUDIT, "audit-logs");
        List<ImagingAuditLog> logs = imagingAuditService.listByImage(id);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit-logs/recent")
    public ResponseEntity<?> recentAuditLogs(
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW_AUDIT, "audit-logs-recent");
        return ResponseEntity.ok(imagingAuditService.listRecent());
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(
            @PathVariable Long id,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_VIEW, "preview");
            MedicalImage image = medicalImageService.getById(id);
            String pathStr = image.getPreviewPath() != null ? image.getPreviewPath() : image.getFilePath();
            Path path = Paths.get(pathStr);
            Resource resource;
            String contentType = "image/png";
            if (Boolean.TRUE.equals(image.getFileEncrypted()) && image.getPreviewPath() == null) {
                byte[] bytes = dataEncryptionService.decryptFileBytes(path);
                resource = new ByteArrayResource(bytes);
            } else {
                resource = new UrlResource(path.toUri());
                if (!resource.exists() || !resource.isReadable()) {
                    return ResponseEntity.notFound().build();
                }
                if (image.getContentType() != null && image.getPreviewPath() == null) {
                    contentType = image.getContentType();
                }
            }
            imagingAuditService.log(parseOptionalUserId(userIdHeader), role, "PREVIEW", "MEDICAL_IMAGE",
                    id, id, null, true, request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview\"")
                    .header("X-AI-Disclaimer", "NOT_A_DIAGNOSIS")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_VIEW_PHI, "download");
            MedicalImage image = medicalImageService.getById(id);
            Path path = Paths.get(image.getFilePath());
            Resource resource;
            if (Boolean.TRUE.equals(image.getFileEncrypted())) {
                byte[] bytes = dataEncryptionService.decryptFileBytes(path);
                resource = new ByteArrayResource(bytes);
            } else {
                resource = new UrlResource(path.toUri());
                if (!resource.exists() || !resource.isReadable()) {
                    return ResponseEntity.notFound().build();
                }
            }
            imagingAuditService.log(parseOptionalUserId(userIdHeader), role, "DOWNLOAD", "MEDICAL_IMAGE",
                    id, id, null, true, request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + image.getOriginalFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/sync-pacs")
    public ResponseEntity<?> syncPacs(
            @PathVariable Long id,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_SYNC_EXTERNAL, "sync-pacs");
            Map<String, Object> result = medicalImageService.syncToPacs(id);
            imagingAuditService.log(parseOptionalUserId(userIdHeader), role, "SYNC_PACS", "MEDICAL_IMAGE",
                    id, id, String.valueOf(result.get("accessionNumber")), true, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/sync-ehr")
    public ResponseEntity<?> syncEhr(
            @PathVariable Long id,
            @RequestParam(value = "analysisResultId", required = false) Long analysisResultId,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_SYNC_EXTERNAL, "sync-ehr");
            Map<String, Object> result = medicalImageService.syncToEhr(id, analysisResultId);
            imagingAuditService.log(parseOptionalUserId(userIdHeader), role, "SYNC_EHR", "MEDICAL_IMAGE",
                    id, id, String.valueOf(result.get("documentId")), true, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pacs/query")
    public ResponseEntity<?> pacsQuery(
            @RequestParam(value = "patientAlias", required = false) String patientAlias,
            @RequestParam(value = "accessionNumber", required = false) String accessionNumber,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW, "pacs-query");
        return ResponseEntity.ok(pacsIntegrationService.queryStudy(patientAlias, accessionNumber));
    }

    public record ReviewRequest(
            Long analysisResultId,
            String decision,
            String physicianComment,
            String confirmedFindingsText,
            String falsePositiveNotes,
            String missedFindingNotes,
            boolean acknowledgedNonDiagnostic
    ) {}

    private Long parseOptionalUserId(String header) {
        if (header == null || header.isBlank()) return null;
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
