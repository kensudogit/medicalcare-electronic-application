# フロントエンド用Dockerfile - Next.jsアプリケーションのコンテナ化設定

# ベースイメージ - Node.js 18（Alpine Linux版、軽量）
FROM node:18-alpine

# 作業ディレクトリの設定
WORKDIR /app

# パッケージファイルのコピー
COPY package*.json ./

# 依存関係のインストール（devDependenciesも含む）
RUN npm install

# ソースコードのコピー
COPY . .

# アプリケーションのビルド
RUN npm run build

# ポートの公開設定
EXPOSE 3000

# 環境変数の設定
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000

# アプリケーション起動コマンド
CMD ["npm", "start"] 