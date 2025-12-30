# Medical Care Electronic Application - Frontend

医療電子申請システムのフロントエンドアプリケーションです。

## 技術スタック

- **Framework**: Next.js 14
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Deployment**: Vercel

## セットアップ

### 前提条件

- Node.js 18.x以上
- npm または yarn

### インストール

```bash
# 依存関係をインストール
npm install

# 開発サーバーを起動
npm run dev

# 本番ビルド
npm run build

# 本番サーバーを起動
npm start
```

## 開発

### 開発サーバー

```bash
npm run dev
```

http://localhost:3000 でアプリケーションにアクセスできます。

### ビルド

```bash
npm run build
```

### リント

```bash
npm run lint
```

## デプロイ

### Vercel

1. Vercel CLIをインストール
```bash
npm install -g vercel
```

2. ログイン
```bash
vercel login
```

3. デプロイ
```bash
vercel --prod
```

### 環境変数

以下の環境変数を設定してください：

- `NODE_ENV`: production
- `NEXT_PUBLIC_API_BASE_URL`: バックエンドAPIのURL
- `NEXT_PUBLIC_APP_NAME`: アプリケーション名
- `NEXT_PUBLIC_APP_VERSION`: アプリケーションバージョン

## プロジェクト構造

```
src/
├── pages/          # ページコンポーネント
│   ├── api/        # APIルート
│   ├── _app.tsx    # アプリケーションラッパー
│   └── index.tsx   # ホームページ
├── styles/         # スタイルファイル
│   └── globals.css # グローバルスタイル
└── components/     # 再利用可能なコンポーネント
```

## 機能

- レスポンシブデザイン
- ダークモード対応
- アクセシビリティ対応
- SEO最適化
- パフォーマンス最適化

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。
