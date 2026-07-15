package com.medicalcare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.medicalcare.domain.dao.ImageAnalysisResultDao;
import com.medicalcare.domain.dao.MedicalImageDao;
import com.medicalcare.domain.entity.ImageAnalysisResult;
import com.medicalcare.domain.entity.MedicalImage;
import com.medicalcare.security.DataEncryptionService;
import com.medicalcare.security.PhiAnonymizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 医療画像アップロード・解析サービス（匿名化・暗号化対応）
 */
@Service
public class MedicalImageService {

    private final MedicalImageDao medicalImageDao;
    private final ImageAnalysisResultDao imageAnalysisResultDao;
    private final AiImagingClient aiImagingClient;
    private final PacsIntegrationService pacsIntegrationService;
    private final EhrIntegrationService ehrIntegrationService;
    private final PhiAnonymizationService phiAnonymizationService;
    private final DataEncryptionService dataEncryptionService;

    @Value("${file.upload.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> DICOM_EXTENSIONS = Set.of(".dcm", ".dicom");

    public MedicalImageService(MedicalImageDao medicalImageDao,
                               ImageAnalysisResultDao imageAnalysisResultDao,
                               AiImagingClient aiImagingClient,
                               PacsIntegrationService pacsIntegrationService,
                               EhrIntegrationService ehrIntegrationService,
                               PhiAnonymizationService phiAnonymizationService,
                               DataEncryptionService dataEncryptionService) {
        this.medicalImageDao = medicalImageDao;
        this.imageAnalysisResultDao = imageAnalysisResultDao;
        this.aiImagingClient = aiImagingClient;
        this.pacsIntegrationService = pacsIntegrationService;
        this.ehrIntegrationService = ehrIntegrationService;
        this.phiAnonymizationService = phiAnonymizationService;
        this.dataEncryptionService = dataEncryptionService;
    }

    public MedicalImage upload(MultipartFile file,
                               String modality,
                               Long medicalInstitutionId,
                               Long applicationId,
                               Long uploadedByUserId,
                               String patientId,
                               String patientName) {
        try {
            String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "upload.bin");
            String ext = extractExtension(originalName).toLowerCase(Locale.ROOT);

            Path dir = Paths.get(uploadDir, "medical-images");
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? ".bin" : ext);
            Path dest = dir.resolve(storedName);
            file.transferTo(dest.toFile());

            boolean encrypted = false;
            if (dataEncryptionService.isEnabled()) {
                Path encDest = dir.resolve(storedName + ".enc");
                dataEncryptionService.encryptFile(dest, encDest);
                Files.deleteIfExists(dest);
                dest = encDest;
                storedName = encDest.getFileName().toString();
                encrypted = true;
            }

            boolean isDicom = DICOM_EXTENSIONS.contains(ext) || isLikelyDicom(originalName);
            String resolvedModality = (modality != null && !modality.isBlank())
                    ? modality.toUpperCase(Locale.ROOT)
                    : guessModality(originalName, isDicom);

            String rawPatientId = patientId;
            String rawPatientName = patientName;

            MedicalImage image = new MedicalImage();
            image.setOriginalFileName(sanitizeFileName(originalName));
            image.setStoredFileName(storedName);
            image.setFilePath(dest.toAbsolutePath().toString());
            image.setContentType(file.getContentType());
            image.setFileSize(file.getSize());
            image.setIsDicom(isDicom);
            image.setModality(resolvedModality);
            image.setMedicalInstitutionId(medicalInstitutionId);
            image.setApplicationId(applicationId);
            image.setUploadedByUserId(uploadedByUserId);
            image.setFileEncrypted(encrypted);
            image.setStatus("UPLOADED");

            applyPatientAnonymization(image, rawPatientId, rawPatientName);

            if (isDicom) {
                try {
                    Path metaSource = dest;
                    Path tempPlain = null;
                    if (encrypted) {
                        tempPlain = Files.createTempFile("dicom-meta-", ".dcm");
                        Files.write(tempPlain, dataEncryptionService.decryptFileBytes(dest));
                        metaSource = tempPlain;
                    }
                    JsonNode meta = aiImagingClient.extractDicomMetadata(metaSource.toFile());
                    if (meta.hasNonNull("patient_id") && (rawPatientId == null || rawPatientId.isBlank())) {
                        rawPatientId = meta.get("patient_id").asText();
                    }
                    if (meta.hasNonNull("patient_name") && (rawPatientName == null || rawPatientName.isBlank())) {
                        rawPatientName = meta.get("patient_name").asText();
                    }
                    applyPatientAnonymization(image, rawPatientId, rawPatientName);

                    String anonymizedMeta = phiAnonymizationService.anonymizeDicomMetadataJson(meta.toString());
                    if (dataEncryptionService.isEnabled()) {
                        image.setDicomMetadata(dataEncryptionService.encryptText(anonymizedMeta));
                        image.setPhiEncrypted(true);
                    } else {
                        image.setDicomMetadata(anonymizedMeta);
                    }
                    if (meta.hasNonNull("study_instance_uid")) {
                        image.setStudyUid(meta.get("study_instance_uid").asText());
                    }
                    if (meta.hasNonNull("series_instance_uid")) {
                        image.setSeriesUid(meta.get("series_instance_uid").asText());
                    }
                    if (meta.hasNonNull("sop_instance_uid")) {
                        image.setSopInstanceUid(meta.get("sop_instance_uid").asText());
                    }
                    if (meta.hasNonNull("modality") && (modality == null || modality.isBlank())) {
                        image.setModality(mapDicomModality(meta.get("modality").asText()));
                    }
                    if (tempPlain != null) {
                        Files.deleteIfExists(tempPlain);
                    }
                } catch (Exception ignored) {
                    // AIサービス未起動時はメタデータなしで継続
                }
            }

            // 平文患者IDはDBに残さない
            image.setPatientId(null);
            return medicalImageDao.save(image);
        } catch (Exception e) {
            throw new RuntimeException("画像アップロードに失敗しました: " + e.getMessage(), e);
        }
    }

    public ImageAnalysisResult analyze(Long imageId, String provider, boolean generateFindings, String patientContext) {
        MedicalImage image = medicalImageDao.findById(imageId)
                .orElseThrow(() -> new RuntimeException("画像が見つかりません: " + imageId));

        image.setStatus("ANALYZING");
        medicalImageDao.save(image);

        ImageAnalysisResult result = new ImageAnalysisResult();
        result.setMedicalImageId(imageId);
        result.setProvider(provider != null ? provider : "inhouse");
        result.setModality(image.getModality());
        result.setIsDiagnosticClaim(false);
        result.setDisclaimerText(PhysicianReviewService.DISCLAIMER);
        result.setReviewStatus("PENDING_REVIEW");

        Path tempPlain = null;
        try {
            Path analyzePath = Paths.get(image.getFilePath());
            if (Boolean.TRUE.equals(image.getFileEncrypted())) {
                tempPlain = Files.createTempFile("analyze-", ".bin");
                Files.write(tempPlain, dataEncryptionService.decryptFileBytes(analyzePath));
                analyzePath = tempPlain;
            }

            JsonNode response = aiImagingClient.analyze(
                    analyzePath.toFile(),
                    image.getModality(),
                    provider,
                    generateFindings,
                    patientContext
            );

            result.setModelVersion(textOrNull(response, "model_version"));
            String findings = textOrNull(response, "findings_text");
            if (findings != null) {
                findings = "[診断支援候補・確定診断ではありません]\n" + findings
                        + "\n\n" + PhysicianReviewService.DISCLAIMER;
            }
            result.setFindingsText(findings);
            result.setProcessingMs(response.has("processing_ms") ? response.get("processing_ms").asInt() : null);
            result.setDetectionsJson(response.has("boxes") ? response.get("boxes").toString() : "[]");
            result.setClassificationsJson(
                    response.has("classifications") ? response.get("classifications").toString() : "[]");
            result.setRawResponse(response.toString());
            result.setStatus("COMPLETED");
            result.setIsDiagnosticClaim(false);

            if (response.has("raw") && response.get("raw").hasNonNull("preview_id")) {
                String previewId = response.get("raw").get("preview_id").asText();
                Path previewDest = Paths.get(uploadDir, "medical-images", "previews",
                        image.getId() + "_" + previewId);
                try {
                    aiImagingClient.savePreviewLocally(previewId, previewDest);
                    image.setPreviewPath(previewDest.toAbsolutePath().toString());
                } catch (Exception ignored) {
                }
            }

            image.setStatus("ANALYZED");
            medicalImageDao.save(image);
            return imageAnalysisResultDao.save(result);
        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            image.setStatus("FAILED");
            medicalImageDao.save(image);
            imageAnalysisResultDao.save(result);
            throw new RuntimeException("画像解析に失敗しました: " + e.getMessage(), e);
        } finally {
            if (tempPlain != null) {
                try {
                    Files.deleteIfExists(tempPlain);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public Map<String, Object> syncToPacs(Long imageId) {
        MedicalImage image = getById(imageId);
        Map<String, Object> pacsResult = pacsIntegrationService.sendStudy(image);
        if (pacsResult.containsKey("accessionNumber")) {
            image.setPacsAccessionNumber(String.valueOf(pacsResult.get("accessionNumber")));
            medicalImageDao.save(image);
        }
        return pacsResult;
    }

    public Map<String, Object> syncToEhr(Long imageId, Long analysisResultId) {
        MedicalImage image = getById(imageId);
        ImageAnalysisResult analysis = null;
        if (analysisResultId != null) {
            analysis = imageAnalysisResultDao.findById(analysisResultId).orElse(null);
        } else {
            analysis = imageAnalysisResultDao.findFirstByMedicalImageIdOrderByCreatedAtDesc(imageId).orElse(null);
        }
        if (analysis != null && !"APPROVED_AS_CANDIDATE".equals(analysis.getReviewStatus())
                && !"PHYSICIAN_REVIEWED".equals(image.getStatus())) {
            // EHR送信は医師確認後を推奨（強制は設定可能だが、既定では警告付きで許可）
            Map<String, Object> warned = ehrIntegrationService.sendFindings(image, analysis);
            warned.put("warning", "医師未承認のAI候補を電子カルテへ送信しています。確定診断としては記録しないでください。");
            if (warned.containsKey("documentId")) {
                image.setEhrDocumentId(String.valueOf(warned.get("documentId")));
                medicalImageDao.save(image);
            }
            return warned;
        }
        Map<String, Object> ehrResult = ehrIntegrationService.sendFindings(image, analysis);
        if (ehrResult.containsKey("documentId")) {
            image.setEhrDocumentId(String.valueOf(ehrResult.get("documentId")));
            medicalImageDao.save(image);
        }
        return ehrResult;
    }

    public MedicalImage getById(Long id) {
        return medicalImageDao.findById(id)
                .orElseThrow(() -> new RuntimeException("画像が見つかりません: " + id));
    }

    public MedicalImage sanitizeForRole(MedicalImage image, String role, boolean canViewPhi) {
        // 常に平文患者IDは返さない
        image.setPatientId(null);
        if (!canViewPhi) {
            image.setPatientIdHash(null);
            image.setPatientNameHash(null);
            image.setDicomMetadata(null);
        } else if (Boolean.TRUE.equals(image.getPhiEncrypted()) && image.getDicomMetadata() != null) {
            try {
                image.setDicomMetadata(dataEncryptionService.decryptText(image.getDicomMetadata()));
            } catch (Exception ignored) {
            }
        }
        return image;
    }

    public List<MedicalImage> listAll() {
        return medicalImageDao.findAll();
    }

    public List<MedicalImage> listByInstitution(Long institutionId) {
        return medicalImageDao.findByMedicalInstitutionIdOrderByCreatedAtDesc(institutionId);
    }

    public List<ImageAnalysisResult> listAnalyses(Long imageId) {
        return imageAnalysisResultDao.findByMedicalImageIdOrderByCreatedAtDesc(imageId);
    }

    public Optional<ImageAnalysisResult> latestAnalysis(Long imageId) {
        return imageAnalysisResultDao.findFirstByMedicalImageIdOrderByCreatedAtDesc(imageId);
    }

    public Map<String, Object> getProviders() {
        try {
            return aiImagingClient.listProviders();
        } catch (Exception e) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("default", "inhouse");
            fallback.put("error", e.getMessage());
            fallback.put("providers", List.of(
                    Map.of("id", "inhouse", "name", "自社AIモデル", "available", false),
                    Map.of("id", "sagemaker", "name", "AWS SageMaker", "available", false),
                    Map.of("id", "azure", "name", "Azure AI", "available", false),
                    Map.of("id", "google", "name", "Google Cloud", "available", false),
                    Map.of("id", "external", "name", "外部医療AI API", "available", false)
            ));
            fallback.put("modalities", List.of(
                    "XRAY", "CT", "MRI", "ULTRASOUND", "ENDOSCOPY", "PATHOLOGY", "DICOM", "OTHER"
            ));
            return fallback;
        }
    }

    private void applyPatientAnonymization(MedicalImage image, String patientId, String patientName) {
        image.setPatientAlias(phiAnonymizationService.createDisplayAlias(patientId));
        image.setPatientIdHash(phiAnonymizationService.hashPatientId(patientId));
        image.setPatientNameHash(phiAnonymizationService.hashPatientName(patientName));
        image.setPatientNameMasked(phiAnonymizationService.maskPatientName(patientName));
        if (patientId != null) {
            // 互換: マスク済みのみ一時的に patientId に入れず、alias を使う
        }
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("(?i)(patient|name|id)[_-]?", "img_");
    }

    private String extractExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx) : "";
    }

    private boolean isLikelyDicom(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".dcm") || lower.endsWith(".dicom") || lower.contains("dicom");
    }

    private String guessModality(String filename, boolean isDicom) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (isDicom) return "DICOM";
        if (lower.contains("xray") || lower.contains("chest")) return "XRAY";
        if (lower.contains("ct")) return "CT";
        if (lower.contains("mri") || lower.contains("mr_")) return "MRI";
        if (lower.contains("ultrasound") || lower.contains("echo")) return "ULTRASOUND";
        if (lower.contains("endo")) return "ENDOSCOPY";
        if (lower.contains("path") || lower.contains("histo")) return "PATHOLOGY";
        return "OTHER";
    }

    private String mapDicomModality(String dicomModality) {
        if (dicomModality == null) return "DICOM";
        return switch (dicomModality.toUpperCase(Locale.ROOT)) {
            case "CR", "DX", "XA" -> "XRAY";
            case "CT" -> "CT";
            case "MR" -> "MRI";
            case "US" -> "ULTRASOUND";
            case "ES" -> "ENDOSCOPY";
            case "SM", "XC" -> "PATHOLOGY";
            default -> "DICOM";
        };
    }

    private String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}
