-- 医療系電子申請システム データベーススキーマ

-- 医療機関テーブル
CREATE TABLE medical_institutions (
    id BIGSERIAL PRIMARY KEY,
    institution_code VARCHAR(50) UNIQUE NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    institution_type VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    representative_name VARCHAR(100),
    license_number VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1
);

-- 申請テーブル
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    institution_id BIGINT NOT NULL REFERENCES medical_institutions(id),
    application_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1
);

-- インデックス
CREATE INDEX idx_medical_institutions_institution_code ON medical_institutions(institution_code);
CREATE INDEX idx_medical_institutions_status ON medical_institutions(status);
CREATE INDEX idx_applications_application_number ON applications(application_number);
CREATE INDEX idx_applications_institution_id ON applications(institution_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_application_type ON applications(application_type);
CREATE INDEX idx_applications_submitted_at ON applications(submitted_at);

-- サンプルデータ
INSERT INTO medical_institutions (
    institution_code, 
    institution_name, 
    institution_type, 
    address, 
    phone, 
    email, 
    representative_name, 
    license_number
) VALUES 
('HOSP001', '東京総合病院', '病院', '東京都新宿区西新宿1-1-1', '03-1234-5678', 'info@tokyo-hospital.jp', '田中太郎', 'L123456789'),
('CLINIC001', '青山内科クリニック', 'クリニック', '東京都港区青山1-2-3', '03-9876-5432', 'info@aoyama-clinic.jp', '佐藤花子', 'L987654321'),
('PHARMACY001', '新宿薬局', '薬局', '東京都新宿区歌舞伎町1-4-5', '03-5555-6666', 'info@shinjuku-pharmacy.jp', '山田次郎', 'L111222333');

-- 申請タイプマスタ
CREATE TABLE application_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) UNIQUE NOT NULL,
    type_name VARCHAR(255) NOT NULL,
    description TEXT,
    required_documents TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO application_types (type_code, type_name, description, required_documents) VALUES
('NEW_LICENSE', '新規開業届', '医療機関の新規開業に関する届出', '申請書, 施設概要書, 人員配置表'),
('RENEWAL', '更新申請', '医療機関の許可更新申請', '申請書, 更新理由書'),
('CHANGE_INFO', '変更届', '医療機関情報の変更届出', '申請書, 変更内容証明書'),
('CLOSURE', '廃止届', '医療機関の廃止届出', '申請書, 廃止理由書'); 