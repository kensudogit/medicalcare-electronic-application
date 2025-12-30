# Vercel デプロイ完全ガイド

## 概要

このドキュメントでは、Medical Care Electronic ApplicationをVercelにデプロイして、他のPCからHTTP接続できるようにする手順を説明します。

## 前提条件

- Node.js 18.x以上がインストールされている
- Vercelアカウントがある
- GitHubアカウントがある（推奨）
- プロジェクトがGitリポジトリとして管理されている

## 1. Vercel CLIのインストール

### Windows
```cmd
npm install -g vercel
```

### macOS/Linux
```bash
npm install -g vercel
```

## 2. Vercelにログイン

```bash
vercel login
```

ブラウザが開いてVercelアカウントでのログインを求められます。

## 3. プロジェクトの準備

### 3.1 フロントエンドディレクトリに移動
```bash
cd frontend
```

### 3.2 依存関係をインストール
```bash
npm install
```

### 3.3 プロジェクトをビルド
```bash
npm run build
```

## 4. Vercelにデプロイ

### 4.1 初回デプロイ
```bash
vercel
```

以下の質問に答えてください：
- Set up and deploy? → `Y`
- Which scope? → あなたのアカウントを選択
- Link to existing project? → `N`
- What's your project's name? → `medicalcare-frontend`（または任意の名前）
- In which directory is your code located? → `./`（現在のディレクトリ）
- Want to override the settings? → `N`

### 4.2 本番環境にデプロイ
```bash
vercel --prod
```

## 5. 環境変数の設定

Vercelダッシュボードで以下の環境変数を設定：

1. [Vercel Dashboard](https://vercel.com/dashboard) にアクセス
2. プロジェクトを選択
3. Settings → Environment Variables に移動
4. 以下の環境変数を追加：

| 名前 | 値 | 環境 |
|------|-----|------|
| `NODE_ENV` | `production` | Production, Preview, Development |
| `NEXT_PUBLIC_API_BASE_URL` | `https://your-backend-domain.com` | Production, Preview, Development |
| `NEXT_PUBLIC_APP_NAME` | `Medical Care Electronic Application` | Production, Preview, Development |
| `NEXT_PUBLIC_APP_VERSION` | `1.0.0` | Production, Preview, Development |

## 6. カスタムドメインの設定（オプション）

### 6.1 Vercelダッシュボードで設定
1. Settings → Domains に移動
2. Add Domain をクリック
3. カスタムドメインを入力
4. DNS設定の指示に従って設定

### 6.2 DNS設定例
```
Type: CNAME
Name: www
Value: cname.vercel-dns.com
```

## 7. 自動デプロイの設定

### 7.1 GitHub連携
1. Vercelダッシュボードで Settings → Git に移動
2. Connect Git Repository をクリック
3. GitHubリポジトリを選択
4. 自動デプロイの設定を確認

### 7.2 ブランチ設定
- Production Branch: `main` または `master`
- Preview Branches: `develop`, `feature/*` など

## 8. デプロイの確認

### 8.1 デプロイ状態の確認
```bash
vercel ls
```

### 8.2 ログの確認
```bash
vercel logs
```

### 8.3 アプリケーションの動作確認
デプロイされたURLにアクセスして以下を確認：
- ホームページが表示される
- デプロイ状態確認ページが動作する
- ヘルスチェックAPIが動作する

## 9. トラブルシューティング

### 9.1 ビルドエラー
```bash
# ローカルでビルドテスト
npm run build

# エラーログを確認
npm run build 2>&1 | tee build.log
```

### 9.2 デプロイエラー
```bash
# デプロイログを確認
vercel logs

# 再デプロイ
vercel --prod
```

### 9.3 環境変数の問題
- Vercelダッシュボードで環境変数が正しく設定されているか確認
- 環境変数の値に特殊文字が含まれていないか確認

## 10. セキュリティ設定

### 10.1 セキュリティヘッダー
`vercel.json`で以下のヘッダーが設定済み：
- X-Content-Type-Options
- X-Frame-Options
- X-XSS-Protection
- Referrer-Policy

### 10.2 CORS設定
- 本番環境では適切なオリジンのみ許可
- 開発環境では`*`を許可

## 11. パフォーマンス最適化

### 11.1 画像最適化
- Next.js Image コンポーネントを使用
- WebP/AVIF形式をサポート
- レスポンシブ画像サイズ

### 11.2 バンドル最適化
- コード分割
- ツリーレンダリング
- 遅延読み込み

## 12. 監視とメトリクス

### 12.1 Vercel Analytics
1. Vercelダッシュボードで Analytics を有効化
2. パフォーマンスメトリクスを確認

### 12.2 カスタムメトリクス
- `/api/health` エンドポイントでヘルスチェック
- `/api/vercel-deploy` エンドポイントでデプロイ状態確認

## 13. バックアップと復旧

### 13.1 設定のバックアップ
```bash
# vercel.jsonの内容をバックアップ
cp vercel.json vercel.json.backup

# 環境変数のエクスポート
vercel env ls > env-vars.txt
```

### 13.2 復旧手順
1. 設定ファイルを復元
2. 環境変数を再設定
3. 再デプロイ

## 14. 更新とメンテナンス

### 14.1 定期的な更新
```bash
# 依存関係の更新
npm update

# セキュリティ監査
npm audit

# 古いパッケージの確認
npm outdated
```

### 14.2 デプロイ後の確認
- アプリケーションの動作確認
- パフォーマンスの確認
- エラーログの確認

## 15. サポートとリソース

### 15.1 公式ドキュメント
- [Vercel Documentation](https://vercel.com/docs)
- [Next.js Documentation](https://nextjs.org/docs)

### 15.2 コミュニティ
- [Vercel Community](https://github.com/vercel/vercel/discussions)
- [Next.js Community](https://github.com/vercel/next.js/discussions)

## 完了

これで、Medical Care Electronic ApplicationがVercelにデプロイされ、他のPCからHTTP接続できるようになります。

デプロイされたURLは以下の形式になります：
- デフォルト: `https://your-project-name.vercel.app`
- カスタムドメイン: `https://your-domain.com`

他のPCからアクセスする際は、このURLを使用してください。
