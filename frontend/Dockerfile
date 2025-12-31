# フロントエンド用Dockerfile - Next.jsアプリケーションのコンテナ化設定（性能最適化版）

# ビルドステージ
FROM node:18-alpine AS builder

# 作業ディレクトリの設定
WORKDIR /app

# パッケージファイルのコピー
COPY package*.json ./

# 依存関係のインストール（キャッシュ最適化）
RUN npm install && npm cache clean --force

# ソースコードのコピー
COPY . .

# アプリケーションのビルド
RUN npm run build

# 実行ステージ
FROM node:18-alpine AS runner

# セキュリティ更新と必要なパッケージのインストール
RUN apk update && apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# 非rootユーザーの作成
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001

# 作業ディレクトリの設定
WORKDIR /app

# 必要なファイルをビルドステージからコピー
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

# 権限の設定
RUN chown -R nextjs:nodejs /app

# ユーザーを変更
USER nextjs

# ポートの公開設定
EXPOSE 3000

# 環境変数の設定
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV NODE_OPTIONS="--max-old-space-size=1024"

# ヘルスチェック
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:3000/api/health || exit 1

# アプリケーション起動コマンド
CMD ["node", "server.js"] 