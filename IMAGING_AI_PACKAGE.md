# 医療画像AI認識パッケージ

React/Next.js · Java/Spring Boot · Python/FastAPI · WordPress 連携向けの医療画像認識パッケージです。

## 対象画像

| モダリティ | コード |
|-----------|--------|
| X線 | `XRAY` |
| CT | `CT` |
| MRI | `MRI` |
| 超音波 | `ULTRASOUND` |
| 内視鏡 | `ENDOSCOPY` |
| 病理画像 | `PATHOLOGY` |
| DICOMファイル | `DICOM` |

## 画像認識サービス

| プロバイダー | ID | 設定 |
|-------------|-----|------|
| 自社AIモデル | `inhouse` | `AI_INHOUSE_MODEL_URL` |
| AWS SageMaker | `sagemaker` | `AI_SAGEMAKER_ENDPOINT_NAME` |
| Azure AI | `azure` | `AI_AZURE_AI_ENDPOINT` |
| Google Cloud | `google` | `AI_GCP_ENDPOINT_ID` |
| 外部医療AI API | `external` | `AI_EXTERNAL_MEDICAL_AI_URL` |

未設定時は `AI_ENABLE_MOCK_INFERENCE=true` によりモック推論でデモ動作します。

## 実現処理

1. **画像アップロード** — Spring Boot `POST /api/api/medical-images/upload`
2. **DICOM表示** — FastAPI で PNG プレビュー生成 → フロントで表示
3. **病変候補の検出** — 正規化座標の bounding box
4. **画像分類** — ラベル + 信頼度
5. **検出位置の枠表示** — Canvas オーバーレイ
6. **所見文章の生成** — プロバイダー応答 / モック所見
7. **PACS / 電子カルテ連携** — スタブ API（設定で有効化）

## アーキテクチャ

```
Next.js (/imaging)
    ↓
Spring Boot (MedicalImageController)
    ↓ メタデータ保存 / ファイル保管
FastAPI (ai-imaging-service :8090)
    ↓
Provider adapters (inhouse / SageMaker / Azure / GCP / external)
```

## ディレクトリ

```
ai-imaging-service/          # FastAPI AI推論サービス
src/.../MedicalImage*.java   # Spring Boot ドメイン・API
frontend/src/pages/imaging/  # Next.js UI
frontend/src/components/imaging/
wordpress-plugin/            # WP埋め込みショートコード
scripts/migrate-medical-images.sql
```

## 医療コンプライアンス（重要）

患者匿名化・保存暗号化・権限管理・監査ログ・DICOMweb/PACS・医師承認・
「確定診断ではない」UI設計を実装済みです。詳細は [MEDICAL_COMPLIANCE.md](./MEDICAL_COMPLIANCE.md)。

## 起動方法

### 1. FastAPI（ローカル）

**Python 3.11 または 3.12 を使用してください**（3.14 では Pillow / pydantic のホイールが未対応です）。

```bash
cd ai-imaging-service
py -3.11 -m venv .venv
# Windows: .venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
uvicorn app.main:app --reload --port 8090
```

Docker 利用時はイメージ内で Python 3.11 が使われるため、この制約は不要です。
### 2. DBマイグレーション（既存DB）

```bash
psql -U medicalcare_user -d medicalcare_db -f scripts/migrate-medical-images.sql
```

Docker 新規起動時は `schema.sql` にテーブルが含まれます。  
Docker プロファイルでは `ddl-auto: update` のため自動作成も可能です。

### 3. Spring Boot / Next.js

既存どおりバックエンド・フロントを起動し、ホームから「医療画像AI認識」へ遷移、または:

- UI: http://localhost:3000/imaging （Compose時は 3001）
- AI Health: http://localhost:8090/health

### 4. Docker Compose 一式

```bash
docker compose up -d --build
```

追加サービス: `ai-imaging`（port **8090**）  
Nginx: `/ai-imaging/` → FastAPI

## API 一覧（Spring Boot）

| Method | Path | 説明 |
|--------|------|------|
| GET | `/api/api/medical-images/providers` | プロバイダー一覧 |
| GET | `/api/api/medical-images` | 画像一覧 |
| POST | `/api/api/medical-images/upload` | アップロード |
| POST | `/api/api/medical-images/{id}/analyze` | 解析 |
| GET | `/api/api/medical-images/{id}/preview` | プレビュー |
| POST | `/api/api/medical-images/{id}/sync-pacs` | PACS連携 |
| POST | `/api/api/medical-images/{id}/sync-ehr` | 電子カルテ連携 |

※ `server.servlet.context-path=/api` と Controller の `/api/...` による二重パスは既存規約に準拠。

## FastAPI エンドポイント

| Method | Path | 説明 |
|--------|------|------|
| GET | `/health` | ヘルスチェック |
| GET | `/providers` | プロバイダー |
| POST | `/analyze` | 解析（multipart） |
| POST | `/dicom/metadata` | DICOMメタデータ |
| POST | `/dicom/preview` | DICOM→PNG |

## PACS / EHR 有効化

`application.yml` / 環境変数:

```yaml
integration:
  pacs:
    enabled: true
    remote-host: pacs.example.com
    remote-port: 11112
  ehr:
    enabled: true
    base-url: https://ehr.example.com/fhir
```

現状はスタブ実装（accession / documentId 採番）。実DICOM C-STORE / FHIR DocumentReference は接続先確定後に差し替え。

## WordPress

`wordpress-plugin/medicalcare-imaging-embed` を有効化し、投稿に:

```
[medicalcare_imaging url="http://localhost:3001/imaging" height="900"]
```

## 注意

- AI結果は診断支援であり、最終診断は医師が行ってください。
- 本番では認証・認可・監査・PHI取り扱いポリシーを必ず適用してください。
