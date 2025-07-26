# 医療系電子申請システム

保険医療機関等の申請・届出をオンラインで申請・届出することを可能にするシステムです。

## 機能概要

- 医療機関の登録・管理
- 各種申請の作成・提出・管理
- 申請の承認・却下ワークフロー
- 申請状況の追跡・確認

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

## データベーススキーマ

### medical_institutions テーブル
- 医療機関の基本情報を管理
- 機関コード、機関名、種別、住所、連絡先情報など

### applications テーブル
- 申請情報を管理
- 申請番号、医療機関ID、申請タイプ、ステータス、提出日時など

### application_types テーブル
- 申請タイプのマスタデータ
- 新規開業届、更新申請、変更届、廃止届など

## 開発ガイドライン

### コーディング規約
- Java: Google Java Style Guide準拠
- TypeScript: ESLint + Prettier使用
- コミットメッセージ: Conventional Commits準拠

### テスト
```bash
# バックエンドテスト実行
./gradlew test

# フロントエンドテスト実行
npm test
```

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
