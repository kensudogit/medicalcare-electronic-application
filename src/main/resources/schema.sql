-- 医療系電子申請システム データベーススキーマ

-- ユーザーテーブル
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INSTITUTION_ADMIN', 'USER')),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    medical_institution_id BIGINT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- 医療機関テーブル
CREATE TABLE medical_institutions (
    id BIGSERIAL PRIMARY KEY,
    institution_code VARCHAR(20) UNIQUE NOT NULL,
    institution_name VARCHAR(200) NOT NULL,
    institution_type VARCHAR(50) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100),
    representative_name VARCHAR(100),
    license_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 申請タイプマスタテーブル
CREATE TABLE application_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(20) UNIQUE NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 申請テーブル
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    medical_institution_id BIGINT NOT NULL REFERENCES medical_institutions(id),
    application_type_id BIGINT NOT NULL REFERENCES application_types(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED')),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    submitted_by_user_id BIGINT REFERENCES users(id),
    submitted_at TIMESTAMP,
    approved_by_user_id BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    rejected_by_user_id BIGINT REFERENCES users(id),
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 申請ワークフローテーブル
CREATE TABLE application_workflows (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id),
    current_status VARCHAR(20) NOT NULL CHECK (current_status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED')),
    previous_status VARCHAR(20),
    assigned_to_user_id BIGINT REFERENCES users(id),
    comments TEXT,
    status_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by_user_id BIGINT REFERENCES users(id),
    workflow_type VARCHAR(50),
    step_number INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 申請添付ファイルテーブル
CREATE TABLE application_attachments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id),
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    description TEXT,
    upload_type VARCHAR(20) NOT NULL CHECK (upload_type IN ('REQUIRED', 'OPTIONAL', 'ADDITIONAL')),
    uploaded_by_user_id BIGINT REFERENCES users(id),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN DEFAULT false,
    verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    verification_comments TEXT,
    verified_at TIMESTAMP,
    verified_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 通知テーブル
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN ('APPLICATION_STATUS', 'SYSTEM', 'REMINDER', 'APPROVAL_REQUEST')),
    priority VARCHAR(20) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    action_url VARCHAR(500),
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    scheduled_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 監査ログテーブル
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT')),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(100),
    description TEXT,
    status VARCHAR(20) DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS', 'FAILURE', 'ERROR')),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_medical_institution_id ON users(medical_institution_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

CREATE INDEX idx_medical_institutions_institution_code ON medical_institutions(institution_code);

CREATE INDEX idx_applications_application_number ON applications(application_number);
CREATE INDEX idx_applications_medical_institution_id ON applications(medical_institution_id);
CREATE INDEX idx_applications_application_type_id ON applications(application_type_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_submitted_by_user_id ON applications(submitted_by_user_id);
CREATE INDEX idx_applications_submitted_at ON applications(submitted_at);

CREATE INDEX idx_application_workflows_application_id ON application_workflows(application_id);
CREATE INDEX idx_application_workflows_current_status ON application_workflows(current_status);
CREATE INDEX idx_application_workflows_assigned_to_user_id ON application_workflows(assigned_to_user_id);
CREATE INDEX idx_application_workflows_workflow_type ON application_workflows(workflow_type);

CREATE INDEX idx_application_attachments_application_id ON application_attachments(application_id);
CREATE INDEX idx_application_attachments_upload_type ON application_attachments(upload_type);
CREATE INDEX idx_application_attachments_uploaded_by_user_id ON application_attachments(uploaded_by_user_id);
CREATE INDEX idx_application_attachments_verification_status ON application_attachments(verification_status);
CREATE INDEX idx_application_attachments_file_type ON application_attachments(file_type);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_notification_type ON notifications(notification_type);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_related_entity_id ON notifications(related_entity_id);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_status ON audit_logs(status);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- 初期データ挿入
INSERT INTO application_types (type_code, type_name, description) VALUES
('NEW_OPENING', '新規開業届', '医療機関の新規開業に関する届出'),
('RENEWAL', '更新申請', '医療機関の更新に関する申請'),
('CHANGE', '変更届', '医療機関の変更に関する届出'),
('CLOSURE', '廃止届', '医療機関の廃止に関する届出'),
('STAFF_CHANGE', '従事者変更届', '従事者の変更に関する届出'),
('EQUIPMENT_CHANGE', '設備変更届', '医療設備の変更に関する届出');

-- 管理者ユーザーの作成（パスワードは後で変更が必要）
INSERT INTO users (username, email, password_hash, role, first_name, last_name) VALUES
('admin', 'admin@medicalcare.gov.jp', '$2a$10$dummy.hash.for.admin', 'ADMIN', 'システム', '管理者');

-- 医療画像テーブル
CREATE TABLE medical_images (
    id BIGSERIAL PRIMARY KEY,
    study_uid VARCHAR(128),
    series_uid VARCHAR(128),
    sop_instance_uid VARCHAR(128),
    patient_id VARCHAR(100),
    patient_alias VARCHAR(40),
    patient_id_hash VARCHAR(128),
    patient_name_hash VARCHAR(128),
    patient_name_masked VARCHAR(100),
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
    file_encrypted BOOLEAN DEFAULT false,
    phi_encrypted BOOLEAN DEFAULT false,
    status VARCHAR(30) NOT NULL DEFAULT 'UPLOADED' CHECK (status IN (
        'UPLOADED', 'ANALYZING', 'ANALYZED', 'FAILED',
        'PHYSICIAN_REVIEWED', 'PHYSICIAN_REJECTED', 'NEEDS_SECOND_LOOK'
    )),
    medical_institution_id BIGINT REFERENCES medical_institutions(id),
    application_id BIGINT REFERENCES applications(id),
    uploaded_by_user_id BIGINT REFERENCES users(id),
    pacs_accession_number VARCHAR(100),
    ehr_document_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 画像解析結果テーブル
CREATE TABLE image_analysis_results (
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
    review_status VARCHAR(40) DEFAULT 'PENDING_REVIEW',
    is_diagnostic_claim BOOLEAN NOT NULL DEFAULT false,
    disclaimer_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 医師確認・承認
CREATE TABLE physician_reviews (
    id BIGSERIAL PRIMARY KEY,
    medical_image_id BIGINT NOT NULL REFERENCES medical_images(id) ON DELETE CASCADE,
    analysis_result_id BIGINT NOT NULL REFERENCES image_analysis_results(id) ON DELETE CASCADE,
    reviewer_user_id BIGINT NOT NULL REFERENCES users(id),
    reviewer_role VARCHAR(40),
    decision VARCHAR(40) NOT NULL CHECK (decision IN (
        'PENDING', 'APPROVED_AS_CANDIDATE', 'REJECTED',
        'NEEDS_SECOND_LOOK', 'FALSE_POSITIVE', 'MISSED_FINDING'
    )),
    physician_comment TEXT,
    confirmed_findings_text TEXT,
    false_positive_notes TEXT,
    missed_finding_notes TEXT,
    acknowledged_non_diagnostic BOOLEAN NOT NULL DEFAULT false,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 医療画像操作・監査ログ
CREATE TABLE imaging_audit_logs (
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

CREATE INDEX idx_medical_images_modality ON medical_images(modality);
CREATE INDEX idx_medical_images_status ON medical_images(status);
CREATE INDEX idx_medical_images_patient_id ON medical_images(patient_id);
CREATE INDEX idx_medical_images_patient_alias ON medical_images(patient_alias);
CREATE INDEX idx_medical_images_patient_id_hash ON medical_images(patient_id_hash);
CREATE INDEX idx_medical_images_institution_id ON medical_images(medical_institution_id);
CREATE INDEX idx_medical_images_application_id ON medical_images(application_id);
CREATE INDEX idx_medical_images_study_uid ON medical_images(study_uid);
CREATE INDEX idx_image_analysis_results_image_id ON image_analysis_results(medical_image_id);
CREATE INDEX idx_image_analysis_results_provider ON image_analysis_results(provider);
CREATE INDEX idx_physician_reviews_image_id ON physician_reviews(medical_image_id);
CREATE INDEX idx_physician_reviews_decision ON physician_reviews(decision);
CREATE INDEX idx_imaging_audit_logs_image_id ON imaging_audit_logs(medical_image_id);
CREATE INDEX idx_imaging_audit_logs_action ON imaging_audit_logs(action);
CREATE INDEX idx_imaging_audit_logs_created_at ON imaging_audit_logs(created_at); 