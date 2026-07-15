-- 医療コンプライアンス強化マイグレーション
-- psql -U medicalcare_user -d medicalcare_db -f scripts/migrate-imaging-compliance.sql

ALTER TABLE medical_images ADD COLUMN IF NOT EXISTS patient_alias VARCHAR(40);
ALTER TABLE medical_images ADD COLUMN IF NOT EXISTS patient_id_hash VARCHAR(128);
ALTER TABLE medical_images ADD COLUMN IF NOT EXISTS patient_name_hash VARCHAR(128);
ALTER TABLE medical_images ADD COLUMN IF NOT EXISTS patient_name_masked VARCHAR(100);
ALTER TABLE medical_images ADD COLUMN IF NOT EXISTS file_encrypted BOOLEAN DEFAULT false;
ALTER TABLE medical_images ADD COLUMN IF NOT EXISTS phi_encrypted BOOLEAN DEFAULT false;

ALTER TABLE image_analysis_results ADD COLUMN IF NOT EXISTS review_status VARCHAR(40) DEFAULT 'PENDING_REVIEW';
ALTER TABLE image_analysis_results ADD COLUMN IF NOT EXISTS is_diagnostic_claim BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE image_analysis_results ADD COLUMN IF NOT EXISTS disclaimer_text TEXT;

CREATE TABLE IF NOT EXISTS physician_reviews (
    id BIGSERIAL PRIMARY KEY,
    medical_image_id BIGINT NOT NULL REFERENCES medical_images(id) ON DELETE CASCADE,
    analysis_result_id BIGINT NOT NULL REFERENCES image_analysis_results(id) ON DELETE CASCADE,
    reviewer_user_id BIGINT NOT NULL REFERENCES users(id),
    reviewer_role VARCHAR(40),
    decision VARCHAR(40) NOT NULL,
    physician_comment TEXT,
    confirmed_findings_text TEXT,
    false_positive_notes TEXT,
    missed_finding_notes TEXT,
    acknowledged_non_diagnostic BOOLEAN NOT NULL DEFAULT false,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS imaging_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100),
    user_role VARCHAR(40),
    action VARCHAR(60) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id BIGINT,
    medical_image_id BIGINT,
    details TEXT,
    success BOOLEAN NOT NULL DEFAULT true,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_medical_images_patient_alias ON medical_images(patient_alias);
CREATE INDEX IF NOT EXISTS idx_medical_images_patient_id_hash ON medical_images(patient_id_hash);
CREATE INDEX IF NOT EXISTS idx_physician_reviews_image_id ON physician_reviews(medical_image_id);
CREATE INDEX IF NOT EXISTS idx_imaging_audit_logs_image_id ON imaging_audit_logs(medical_image_id);
CREATE INDEX IF NOT EXISTS idx_imaging_audit_logs_created_at ON imaging_audit_logs(created_at);
