package com.medicalcare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 画像解析結果エンティティ
 */
@Entity
@Table(name = "image_analysis_results")
public class ImageAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medical_image_id", nullable = false)
    private Long medicalImageId;

    @Column(name = "provider", nullable = false, length = 40)
    private String provider;

    @Column(name = "model_version", length = 80)
    private String modelVersion;

    @Column(name = "modality", length = 30)
    private String modality;

    @Column(name = "detections_json", columnDefinition = "TEXT")
    private String detectionsJson;

    @Column(name = "classifications_json", columnDefinition = "TEXT")
    private String classificationsJson;

    @Column(name = "findings_text", columnDefinition = "TEXT")
    private String findingsText;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "processing_ms")
    private Integer processingMs;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "COMPLETED";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** 医師レビュー状態 */
    @Column(name = "review_status", length = 40)
    private String reviewStatus = "PENDING_REVIEW";

    /** 確定診断として表示・主張しないフラグ（常に false を維持） */
    @Column(name = "is_diagnostic_claim", nullable = false)
    private Boolean isDiagnosticClaim = false;

    @Column(name = "disclaimer_text", columnDefinition = "TEXT")
    private String disclaimerText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ImageAnalysisResult() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicalImageId() { return medicalImageId; }
    public void setMedicalImageId(Long medicalImageId) { this.medicalImageId = medicalImageId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public String getDetectionsJson() { return detectionsJson; }
    public void setDetectionsJson(String detectionsJson) { this.detectionsJson = detectionsJson; }

    public String getClassificationsJson() { return classificationsJson; }
    public void setClassificationsJson(String classificationsJson) { this.classificationsJson = classificationsJson; }

    public String getFindingsText() { return findingsText; }
    public void setFindingsText(String findingsText) { this.findingsText = findingsText; }

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }

    public Integer getProcessingMs() { return processingMs; }
    public void setProcessingMs(Integer processingMs) { this.processingMs = processingMs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public Boolean getIsDiagnosticClaim() { return isDiagnosticClaim; }
    public void setIsDiagnosticClaim(Boolean isDiagnosticClaim) { this.isDiagnosticClaim = isDiagnosticClaim; }

    public String getDisclaimerText() { return disclaimerText; }
    public void setDisclaimerText(String disclaimerText) { this.disclaimerText = disclaimerText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
