# Vercel デプロイ手順

## 前提条件
- Node.js 18.x以上がインストールされている
- Vercel CLIがインストールされている
- GitHubアカウントがある

## 1. Vercel CLIのインストール
```bash
npm install -g vercel
```

## 2. Vercelにログイン
```bash
vercel login
```

## 3. プロジェクトのビルド
```bash
npm run build
```

## 4. Vercelにデプロイ
```bash
vercel --prod
```

## 5. 環境変数の設定
Vercelダッシュボードで以下の環境変数を設定：

- `NODE_ENV`: production
- `NEXT_PUBLIC_API_BASE_URL`: バックエンドAPIのURL
- `NEXT_PUBLIC_APP_NAME`: Medical Care Electronic Application
- `NEXT_PUBLIC_APP_VERSION`: 1.0.0

## 6. カスタムドメインの設定（オプション）
Vercelダッシュボードでカスタムドメインを設定できます。

## 7. 自動デプロイの設定
GitHubリポジトリと連携して、mainブランチにプッシュすると自動デプロイされます。

## 注意事項
- バックエンドAPIは別途デプロイが必要です
- 環境変数は適切に設定してください
- CORS設定は本番環境に合わせて調整してください
