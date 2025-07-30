/** @type {import('next').NextConfig} */
// Next.js設定ファイル - フロントエンドアプリケーションの設定を定義

const nextConfig = {
  reactStrictMode: true,  // React Strict Mode有効化（開発時の警告強化）
  swcMinify: true,  // SWCミニファイ有効化（高速なビルド処理）
  
  // APIプロキシ設定 - フロントエンドからバックエンドAPIへのリクエスト転送
  async rewrites() {
    return [
      {
        source: '/api/:path*',  // フロントエンドのAPIパス
        destination: 'http://localhost:8080/api/:path*',  // バックエンドAPIの宛先
      },
    ];
  },
};

module.exports = nextConfig; 