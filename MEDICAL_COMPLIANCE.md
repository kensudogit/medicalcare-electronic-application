# 医療画像AI — コンプライアンス・安全設計

本パッケージは診断支援（CDS）用途を想定しています。**確定診断装置・医療機器としての販売・使用には別途規制対応が必要**です。

## 実装済み対策

| 要件 | 実装 |
|------|------|
| 患者氏名・患者IDの匿名化 | `PhiAnonymizationService`：ハッシュ／表示エイリアス／マスク。平文患者IDはDB非保存。DICOMメタデータ匿名化 |
| 通信の暗号化 | TLS（HTTPS）をNginx/LBで終端する前提。`application.yml` に SSL 設定例 |
| 保存データの暗号化 | `DataEncryptionService`（AES-256-GCM）。画像ファイル・PHIメタデータを at-rest 暗号化 |
| アクセス権限管理 | `ImagingRoles` + `ImagingAccessGuard`（`X-User-Id` / `X-User-Role`）。アップロード／解析／承認／PHI閲覧を分離 |
| 操作ログ・監査ログ | `imaging_audit_logs` + `ImagingAuditService`（閲覧・解析・承認・PACS/EHR連携を記録） |
| DICOM／DICOMweb | `/api/dicomweb`：QIDO-RS / WADO-RS / STOW-RS。FastAPI で DICOM 匿名化 |
| PACS連携 | `PacsIntegrationService`（DIMSE/DICOMweb、匿名エイリアス送信） |
| AIを確定診断として表示しない | `isDiagnosticClaim=false` 強制、免責文付与、UIに NOT A DIAGNOSIS 表示 |
| 医師による確認と承認 | `physician_reviews` + 承認API。免責同意チェック必須 |
| 誤検出・見逃し前提UI | SafetyBanner、枠ON/OFF、FP/FNメモ欄、信頼度の注意書き |

## アクセスロール

| ロール | アップロード | 解析 | 閲覧 | PHI/ダウンロード | 医師承認 | 監査ログ | PACS/EHR |
|--------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| ADMIN | ○ | ○ | ○ | ○ | ○ | ○ | ○ |
| PHYSICIAN / RADIOLOGIST | ○ | ○ | ○ | ○ | ○ | - | ○ |
| TECHNICIAN | ○ | ○ | ○ | - | - | - | - |
| INSTITUTION_ADMIN | ○ | ○ | ○ | - | - | ○ | ○ |
| USER | - | - | ○ | - | - | - | - |

## 医療機器該当性・関連法令（確認チェックリスト）

> 以下は開発チーム／薬事・法務向けの確認リストです。本ソフトウェアはデフォルトで「診断支援・研究・院内検証」想定です。

### 日本

- [ ] **医薬品医療機器等法（薬機法）**: プログラム医療機器（SaMD）該当性の評価
- [ ] **医療機器の定義**: 「疾病の診断・治療・予防に使用されることが目的」か（該当する場合は承認・認証・届出）
- [ ] **PMDA / 厚生労働省**: クラス分類、QMS省令、市販後安全対策
- [ ] **個人情報保護法 / 次世代医療基盤法**: 要配慮個人情報（病歴・診療情報）の取扱い
- [ ] **医療情報システムの安全管理に関するガイドライン**（厚労省）: アクセス制御、監査、暗号化、バックアップ
- [ ] **3省2ガイドライン**（厚労省・経産省・総務省）クラウド利用時の対策

### 国際（参考）

- [ ] **EU MDR / MDR Rule 11**: ソフトウェア医療機器分類
- [ ] **FDA**: CDS guidance / 510(k) / De Novo（診断出力の提示方法に注意）
- [ ] **HIPAA**（米国）: PHI の暗号化・監査・BAA
- [ ] **DICOM PS3.15**: 機密保持プロファイル（匿名化）

### 本システムの設計上の位置づけ（現状）

1. UI・APIとも **「確定診断ではない」** ことを明示し、診断クレームフラグを常に `false` に固定
2. 医師承認は **「候補としての妥当性確認」** であり、電子カルテ上の診断名確定は別プロセス
3. 実運用・販売前に、薬事・法務・情報セキュリティ部門による正式判定が必須
4. 医療機器として上市する場合は、臨床評価・リスクマネジメント（ISO 14971）・ソフトウェアライフサイクル（IEC 62304）等が別途必要

## 運用推奨

1. 本番では `PHI_PEPPER` と `ENCRYPTION_KEY_BASE64` を秘密管理（KMS/Vault）
2. 全通信を HTTPS のみ許可（HSTS）
3. `X-User-*` ヘッダを JWT/OIDC クレームに置換
4. 監査ログの改ざん防止（WORM / SIEM 転送）
5. PACS/EHR 有効化前に接続先の匿名化ポリシーを合意

## 関連ファイル

- `src/main/java/com/medicalcare/security/*`
- `src/main/java/com/medicalcare/controller/DicomWebController.java`
- `src/main/java/com/medicalcare/service/PhysicianReviewService.java`
- `frontend/src/components/imaging/SafetyBanner.tsx`
- `frontend/src/components/imaging/PhysicianApprovalPanel.tsx`
- `scripts/migrate-imaging-compliance.sql`
- `IMAGING_AI_PACKAGE.md`
