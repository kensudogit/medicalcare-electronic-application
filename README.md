# 医療系電子申請システム

保険医療機関等の申請・届出をオンラインで申請・届出することを可能にするシステムです。

## 機能概要

### 基本機能
- 医療機関の登録・管理
- 各種申請の作成・提出・管理
- 申請の承認・却下ワークフロー
- 申請状況の追跡・確認

### 医療画像AI認識パッケージ
- X線 / CT / MRI / 超音波 / 内視鏡 / 病理 / DICOM 対応
- 自社AI・SageMaker・Azure AI・Google Cloud・外部医療AI API
- 画像アップロード、DICOM表示、病変検出、分類、枠表示、所見生成、PACS/電子カルテ連携
- 詳細は [IMAGING_AI_PACKAGE.md](./IMAGING_AI_PACKAGE.md)

### 新規追加機能
- **ユーザー認証・認可システム**
  - ロールベースアクセス制御（ADMIN, INSTITUTION_ADMIN, USER）
  - セキュアなパスワード管理
  - セッション管理

- **申請ワークフロー管理**
  - 多段階承認プロセス
  - 担当者割り当て・変更機能
  - ワークフロー状態の追跡
  - コメント・履歴管理

- **ファイルアップロード機能**
  - 多様なファイル形式対応（PDF, JPG, PNG, DOC, DOCX）
  - ファイル検証・承認プロセス
  - セキュアなファイル管理
  - アップロードタイプ管理（必須・任意・追加）

- **通知システム**
  - リアルタイム通知
  - 優先度別通知管理
  - スケジュール通知
  - 通知履歴管理

- **監査ログ機能**
  - 全操作の詳細ログ記録
  - セキュリティ監査レポート
  - 変更履歴追跡
  - コンプライアンス対応

- **レポート機能**
  - 申請統計レポート
  - ワークフロー分析
  - 監査レポート
  - ダッシュボード機能

## 技術スタック

### バックエンド
- **言語**: Java 17
- **フレームワーク**: Spring Boot 3.2.0
- **データベース**: PostgreSQL
- **データベースアクセス**: Doma2
- **パッケージ管理**: Gradle

### フロントエンド
- **フレームワーク**: React 18 + Next.js 14
- **スタイリング**: Tailwind CSS
- **言語**: TypeScript

## プロジェクト構造

```
medicalcare-electronic-application/
├── src/main/java/com/medicalcare/
│   ├── MedicalCareApplication.java          # メインアプリケーションクラス
│   ├── controller/                          # REST API コントローラー
│   │   ├── MedicalInstitutionController.java
│   │   └── ApplicationController.java
│   ├── service/                             # ビジネスロジック
│   │   ├── MedicalInstitutionService.java
│   │   └── ApplicationService.java
│   └── domain/                              # ドメインモデル
│       ├── entity/                          # エンティティ
│       │   ├── MedicalInstitution.java
│       │   └── Application.java
│       └── dao/                             # データアクセスオブジェクト
│           ├── MedicalInstitutionDao.java
│           └── ApplicationDao.java
├── src/main/resources/
│   ├── application.yml                      # アプリケーション設定
│   ├── schema.sql                           # データベーススキーマ
│   └── META-INF/sql/
│       └── doma2-config.properties          # Doma2設定
├── frontend/                                # フロントエンド
│   ├── src/app/
│   │   ├── page.tsx                         # ダッシュボード
│   │   ├── institutions/page.tsx            # 医療機関管理
│   │   └── applications/page.tsx            # 申請管理
│   ├── package.json
│   └── tailwind.config.js
├── build.gradle                             # Gradle設定
└── README.md
```

## セットアップ手順

### 前提条件
- Java 17以上
- Node.js 18以上
- PostgreSQL 12以上

### 1. データベースのセットアップ

```sql
-- PostgreSQLにデータベースを作成
CREATE DATABASE medicalcare_db;
CREATE USER medicalcare_user WITH PASSWORD 'medicalcare_password';
GRANT ALL PRIVILEGES ON DATABASE medicalcare_db TO medicalcare_user;
```

### 2. バックエンドのセットアップ

```bash
# プロジェクトディレクトリに移動
cd devlop/medicalcare-electronic-application

# 依存関係をインストール
./gradlew build

# アプリケーションを起動
./gradlew bootRun
```

バックエンドは `http://localhost:8080` で起動します。

### 3. フロントエンドのセットアップ

```bash
# フロントエンドディレクトリに移動
cd frontend

# 依存関係をインストール
npm install

# 開発サーバーを起動
npm run dev
```

フロントエンドは `http://localhost:3000` で起動します。

## API エンドポイント

### 認証・認可管理
- `POST /api/auth/register` - ユーザー登録
- `POST /api/auth/login` - ユーザー認証（ログイン）
- `PUT /api/auth/users/{userId}` - ユーザー情報更新
- `POST /api/auth/users/{userId}/change-password` - パスワード変更
- `POST /api/auth/users/{userId}/deactivate` - ユーザー無効化
- `GET /api/auth/medical-institutions/{medicalInstitutionId}/users` - 医療機関のユーザー一覧
- `GET /api/auth/users/role/{role}` - ロール別ユーザー一覧
- `GET /api/auth/users/active` - アクティブユーザー一覧

### 医療機関管理
- `GET /api/medical-institutions` - 全医療機関取得
- `GET /api/medical-institutions/{id}` - 医療機関詳細取得
- `POST /api/medical-institutions` - 医療機関登録
- `PUT /api/medical-institutions/{id}` - 医療機関更新
- `DELETE /api/medical-institutions/{id}` - 医療機関削除

### 申請管理
- `GET /api/applications` - 全申請取得
- `GET /api/applications/{id}` - 申請詳細取得
- `POST /api/applications` - 申請作成
- `PUT /api/applications/{id}` - 申請更新
- `DELETE /api/applications/{id}` - 申請削除
- `POST /api/applications/{id}/submit` - 申請提出
- `POST /api/applications/{id}/approve` - 申請承認
- `POST /api/applications/{id}/reject` - 申請却下

### ワークフロー管理
- `POST /api/workflows` - ワークフロー作成
- `PUT /api/workflows/{workflowId}/status` - ワークフロー状態更新
- `PUT /api/workflows/{workflowId}/reassign` - ワークフロー担当者変更
- `GET /api/workflows/application/{applicationId}` - 申請IDによるワークフロー取得
- `GET /api/workflows/status/{status}` - ステータス別ワークフロー一覧
- `GET /api/workflows/assigned/{assignedToUserId}` - 担当者別ワークフロー一覧
- `GET /api/workflows/type/{workflowType}` - ワークフロータイプ別一覧
- `GET /api/workflows/stats/summary` - ワークフロー統計サマリー

### ファイル管理
- `POST /api/files/upload` - ファイルアップロード
- `GET /api/files/download/{attachmentId}` - ファイルダウンロード
- `DELETE /api/files/{attachmentId}` - ファイル削除
- `POST /api/files/{attachmentId}/verify` - ファイル検証
- `GET /api/files/application/{applicationId}` - 申請IDによる添付ファイル一覧
- `GET /api/files/upload-type/{uploadType}` - アップロードタイプ別ファイル一覧
- `GET /api/files/verification-status/{verificationStatus}` - 検証ステータス別ファイル一覧
- `GET /api/files/stats/summary` - ファイル統計サマリー

### 通知管理
- `POST /api/notifications` - 通知作成
- `POST /api/notifications/with-entity` - 関連エンティティ付き通知作成
- `POST /api/notifications/priority` - 優先度付き通知作成
- `POST /api/notifications/scheduled` - スケジュール通知作成
- `POST /api/notifications/{notificationId}/read` - 通知既読化
- `POST /api/notifications/mark-multiple-read` - 複数通知既読化
- `DELETE /api/notifications/{notificationId}` - 通知削除
- `GET /api/notifications/user/{userId}` - ユーザーの通知一覧
- `GET /api/notifications/user/{userId}/unread` - ユーザーの未読通知一覧
- `GET /api/notifications/stats/summary` - 通知統計サマリー

### 監査ログ管理
- `GET /api/audit/user/{userId}` - ユーザー別監査ログ
- `GET /api/audit/action/{action}` - アクション別監査ログ
- `GET /api/audit/entity-type/{entityType}` - エンティティタイプ別監査ログ
- `GET /api/audit/entity-id/{entityId}` - エンティティID別監査ログ
- `GET /api/audit/status/{status}` - ステータス別監査ログ
- `GET /api/audit/date-range` - 日付範囲別監査ログ
- `GET /api/audit/recent` - 最近の監査ログ
- `GET /api/audit/security-report` - セキュリティ監査レポート
- `GET /api/audit/stats/summary` - 監査ログ統計サマリー

## データベーススキーマ

### users テーブル
- ユーザー認証・認可情報を管理
- ユーザー名、メールアドレス、パスワードハッシュ、ロール、医療機関IDなど

### medical_institutions テーブル
- 医療機関の基本情報を管理
- 機関コード、機関名、種別、住所、連絡先情報など

### application_types テーブル
- 申請タイプのマスタデータ
- 新規開業届、更新申請、変更届、廃止届など

### applications テーブル
- 申請情報を管理
- 申請番号、医療機関ID、申請タイプ、ステータス、提出日時など

### application_workflows テーブル
- 申請ワークフロー情報を管理
- 現在のステータス、前のステータス、担当者、コメント、変更履歴など

### application_attachments テーブル
- 申請添付ファイル情報を管理
- ファイル名、パス、サイズ、検証ステータス、アップロード情報など

### notifications テーブル
- 通知情報を管理
- タイトル、メッセージ、通知タイプ、優先度、既読状態、スケジュール情報など

### audit_logs テーブル
- 監査ログ情報を管理
- 操作アクション、エンティティ情報、変更前後の値、IPアドレス、セッション情報など

## 開発ガイドライン

### コーディング規約
- Java: Google Java Style Guide準拠
- TypeScript: ESLint + Prettier使用
- コミットメッセージ: Conventional Commits準拠

### セキュリティガイドライン
- パスワードは必ずハッシュ化して保存
- 認証・認可チェックを全APIで実装
- 監査ログを全操作で記録
- ファイルアップロード時のセキュリティチェック
- SQLインジェクション対策（Doma2使用）
- XSS対策の実装

### テスト
```bash
# バックエンドテスト実行
./gradlew test

# フロントエンドテスト実行
npm test
```

### 監査・ログ管理
- 全操作の監査ログ記録
- セキュリティ監査レポートの定期生成
- ログローテーション設定
- 監査ログの長期保存

## デプロイメント

### Docker化
```bash
# バックエンドのDockerイメージ作成
./gradlew bootBuildImage

# フロントエンドのDockerイメージ作成
cd frontend
docker build -t medicalcare-frontend .
```

### 本番環境設定
- 環境変数でデータベース接続情報を設定
- HTTPS通信の有効化
- ログ出力の設定
- 監視・アラートの設定

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## サポート

技術的な質問や問題がある場合は、GitHubのIssuesページでお知らせください。 
