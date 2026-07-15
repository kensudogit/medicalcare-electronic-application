package com.medicalcare.controller;

import com.medicalcare.domain.entity.MedicalImage;
import com.medicalcare.security.ImagingAccessGuard;
import com.medicalcare.security.ImagingRoles;
import com.medicalcare.service.ImagingAuditService;
import com.medicalcare.service.MedicalImageService;
import com.medicalcare.service.PacsIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * DICOMweb エンドポイント（QIDO-RS / WADO-RS / STOW-RS）
 * 仕様: DICOM PS3.18
 */
@RestController
@RequestMapping("/api/dicomweb")
@CrossOrigin(origins = "*")
public class DicomWebController {

    private final PacsIntegrationService pacsIntegrationService;
    private final MedicalImageService medicalImageService;
    private final ImagingAccessGuard accessGuard;
    private final ImagingAuditService imagingAuditService;

    public DicomWebController(PacsIntegrationService pacsIntegrationService,
                              MedicalImageService medicalImageService,
                              ImagingAccessGuard accessGuard,
                              ImagingAuditService imagingAuditService) {
        this.pacsIntegrationService = pacsIntegrationService;
        this.medicalImageService = medicalImageService;
        this.accessGuard = accessGuard;
        this.imagingAuditService = imagingAuditService;
    }

    /**
     * QIDO-RS: Search for Studies
     * GET /studies?PatientID=...&StudyInstanceUID=...&Modality=...
     * ※ PatientID は匿名ハッシュまたはエイリアスを想定
     */
    @GetMapping(value = "/studies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> qidoStudies(
            @RequestParam(value = "PatientID", required = false) String patientId,
            @RequestParam(value = "StudyInstanceUID", required = false) String studyUid,
            @RequestParam(value = "Modality", required = false) String modality,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userId,
            HttpServletRequest request) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW, "dicomweb-qido");
        Map<String, Object> result = pacsIntegrationService.qidoSearchStudies(patientId, studyUid, modality);
        imagingAuditService.log(parseUserId(userId), role, "DICOMWEB_QIDO", "DICOM_STUDY",
                null, null, "patient=" + patientId, true, request);
        return ResponseEntity.ok(result);
    }

    /**
     * WADO-RS: Retrieve instance metadata / locator
     */
    @GetMapping(value = "/studies/{studyUid}/series/{seriesUid}/instances/{sopUid}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> wadoInstance(
            @PathVariable String studyUid,
            @PathVariable String seriesUid,
            @PathVariable String sopUid,
            @RequestParam("imageId") Long imageId,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "USER") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userId,
            HttpServletRequest request) {
        accessGuard.require(role, ImagingRoles.CAN_VIEW, "dicomweb-wado");
        Map<String, Object> result = pacsIntegrationService.wadoRetrieveInstance(imageId);
        result.put("requestedStudyUid", studyUid);
        result.put("requestedSeriesUid", seriesUid);
        result.put("requestedSopUid", sopUid);
        imagingAuditService.log(parseUserId(userId), role, "DICOMWEB_WADO", "MEDICAL_IMAGE",
                imageId, imageId, null, true, request);
        return ResponseEntity.ok(result);
    }

    /**
     * STOW-RS: Store instances
     */
    @PostMapping(value = "/studies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> stow(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "modality", required = false) String modality,
            @RequestParam(value = "medicalInstitutionId", required = false) Long medicalInstitutionId,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ROLE, defaultValue = "TECHNICIAN") String role,
            @RequestHeader(value = ImagingAccessGuard.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request) {
        try {
            accessGuard.require(role, ImagingRoles.CAN_UPLOAD, "dicomweb-stow");
            Long userId = parseUserId(userIdHeader);
            MedicalImage image = medicalImageService.upload(
                    file, modality != null ? modality : "DICOM",
                    medicalInstitutionId, null, userId, null, null);
            Map<String, Object> stow = pacsIntegrationService.stowStore(image);
            stow.put("imageId", image.getId());
            stow.put("patientAlias", image.getPatientAlias());
            imagingAuditService.log(userId, role, "DICOMWEB_STOW", "MEDICAL_IMAGE",
                    image.getId(), image.getId(), null, true, request);
            return ResponseEntity.ok(stow);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/capabilities")
    public ResponseEntity<?> capabilities() {
        return ResponseEntity.ok(Map.of(
                "dicomweb", true,
                "qidoRs", true,
                "wadoRs", true,
                "stowRs", true,
                "phiPolicy", "PatientID/PatientName are anonymized; only alias/hash stored",
                "tlsRequired", true,
                "specification", "DICOM PS3.18"
        ));
    }

    private Long parseUserId(String header) {
        if (header == null || header.isBlank()) return null;
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
