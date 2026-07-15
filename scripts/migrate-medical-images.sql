-- 既存DB向け: 医療画像認識パッケージ用マイグレーション
-- 実行例: psql -U medicalcare_user -d medicalcare_db -f scripts/migrate-medical-images.sql

CREATE TABLE IF NOT EXISTS medical_images (
    id BIGSERIAL PRIMARY KEY,
    study_uid VARCHAR(128),
    series_uid VARCHAR(128),
    sop_instance_uid VARCHAR(128),
    patient_id VARCHAR(100),
    modality VARCHAR(30) NOT NULL CHECK (modality IN (
        'XRAY', 'CT', 'MRI', 'ULTRASOUND', 'ENDOSCOPY', 'PATHOLOGY', 'DICOM', 'OTHER'
    )),
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    preview_path TEXT,
    content_type VARCHAR(100),
    file_size BIGINT,
    is_dicom BOOLEAN DEFAULT false,
    dicom_metadata TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'UPLOADED' CHECK (status IN (
        'UPLOADED', 'ANALYZING', 'ANALYZED', 'FAILED'
    )),
    medical_institution_id BIGINT REFERENCES medical_institutions(id),
    application_id BIGINT REFERENCES applications(id),
    uploaded_by_user_id BIGINT REFERENCES users(id),
    pacs_accession_number VARCHAR(100),
    ehr_document_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS image_analysis_results (
    id BIGSERIAL PRIMARY KEY,
    medical_image_id BIGINT NOT NULL REFERENCES medical_images(id) ON DELETE CASCADE,
    provider VARCHAR(40) NOT NULL CHECK (provider IN (
        'inhouse', 'sagemaker', 'azure', 'google', 'external'
    )),
    model_version VARCHAR(80),
    modality VARCHAR(30),
    detections_json TEXT,
    classifications_json TEXT,
    findings_text TEXT,
    raw_response TEXT,
    processing_ms INTEGER,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN (
        'COMPLETED', 'FAILED', 'PENDING'
    )),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_medical_images_modality ON medical_images(modality);
CREATE INDEX IF NOT EXISTS idx_medical_images_status ON medical_images(status);
CREATE INDEX IF NOT EXISTS idx_medical_images_patient_id ON medical_images(patient_id);
CREATE INDEX IF NOT EXISTS idx_medical_images_institution_id ON medical_images(medical_institution_id);
CREATE INDEX IF NOT EXISTS idx_medical_images_application_id ON medical_images(application_id);
CREATE INDEX IF NOT EXISTS idx_medical_images_study_uid ON medical_images(study_uid);
CREATE INDEX IF NOT EXISTS idx_image_analysis_results_image_id ON image_analysis_results(medical_image_id);
CREATE INDEX IF NOT EXISTS idx_image_analysis_results_provider ON image_analysis_results(provider);
