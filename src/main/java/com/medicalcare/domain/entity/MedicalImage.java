package com.medicalcare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 医療画像エンティティ
 */
@Entity
@Table(name = "medical_images")
public class MedicalImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_uid", length = 128)
    private String studyUid;

    @Column(name = "series_uid", length = 128)
    private String seriesUid;

    @Column(name = "sop_instance_uid", length = 128)
    private String sopInstanceUid;

    @Column(name = "patient_id", length = 100)
    private String patientId;

    @Column(name = "modality", nullable = false, length = 30)
    private String modality;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "preview_path", columnDefinition = "TEXT")
    private String previewPath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_dicom")
    private Boolean isDicom = false;

    @Column(name = "dicom_metadata", columnDefinition = "TEXT")
    private String dicomMetadata;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "UPLOADED";

    @Column(name = "medical_institution_id")
    private Long medicalInstitutionId;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "uploaded_by_user_id")
    private Long uploadedByUserId;

    @Column(name = "pacs_accession_number", length = 100)
    private String pacsAccessionNumber;

    @Column(name = "ehr_document_id", length = 100)
    private String ehrDocumentId;

    /** 表示用匿名患者ID（ANON-xxxxxxxx） */
    @Column(name = "patient_alias", length = 40)
    private String patientAlias;

    /** 患者IDの一方向ハッシュ（照合用）。平文患者IDは保持しない */
    @Column(name = "patient_id_hash", length = 128)
    private String patientIdHash;

    /** 患者氏名ハッシュ（照合用） */
    @Column(name = "patient_name_hash", length = 128)
    private String patientNameHash;

    /** マスク済み氏名表示用 */
    @Column(name = "patient_name_masked", length = 100)
    private String patientNameMasked;

    /** 保存ファイルが暗号化されているか */
    @Column(name = "file_encrypted")
    private Boolean fileEncrypted = false;

    /** PHIフィールドが暗号化されているか */
    @Column(name = "phi_encrypted")
    private Boolean phiEncrypted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MedicalImage() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudyUid() { return studyUid; }
    public void setStudyUid(String studyUid) { this.studyUid = studyUid; }

    public String getSeriesUid() { return seriesUid; }
    public void setSeriesUid(String seriesUid) { this.seriesUid = seriesUid; }

    public String getSopInstanceUid() { return sopInstanceUid; }
    public void setSopInstanceUid(String sopInstanceUid) { this.sopInstanceUid = sopInstanceUid; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Boolean getIsDicom() { return isDicom; }
    public void setIsDicom(Boolean isDicom) { this.isDicom = isDicom; }

    public String getDicomMetadata() { return dicomMetadata; }
    public void setDicomMetadata(String dicomMetadata) { this.dicomMetadata = dicomMetadata; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getMedicalInstitutionId() { return medicalInstitutionId; }
    public void setMedicalInstitutionId(Long medicalInstitutionId) { this.medicalInstitutionId = medicalInstitutionId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(Long uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }

    public String getPacsAccessionNumber() { return pacsAccessionNumber; }
    public void setPacsAccessionNumber(String pacsAccessionNumber) { this.pacsAccessionNumber = pacsAccessionNumber; }

    public String getEhrDocumentId() { return ehrDocumentId; }
    public void setEhrDocumentId(String ehrDocumentId) { this.ehrDocumentId = ehrDocumentId; }

    public String getPatientAlias() { return patientAlias; }
    public void setPatientAlias(String patientAlias) { this.patientAlias = patientAlias; }

    public String getPatientIdHash() { return patientIdHash; }
    public void setPatientIdHash(String patientIdHash) { this.patientIdHash = patientIdHash; }

    public String getPatientNameHash() { return patientNameHash; }
    public void setPatientNameHash(String patientNameHash) { this.patientNameHash = patientNameHash; }

    public String getPatientNameMasked() { return patientNameMasked; }
    public void setPatientNameMasked(String patientNameMasked) { this.patientNameMasked = patientNameMasked; }

    public Boolean getFileEncrypted() { return fileEncrypted; }
    public void setFileEncrypted(Boolean fileEncrypted) { this.fileEncrypted = fileEncrypted; }

    public Boolean getPhiEncrypted() { return phiEncrypted; }
    public void setPhiEncrypted(Boolean phiEncrypted) { this.phiEncrypted = phiEncrypted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
