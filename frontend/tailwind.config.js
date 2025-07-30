/** @type {import('tailwindcss').Config} */
// Tailwind CSS設定ファイル - CSSフレームワークの設定を定義

module.exports = {
  // コンテンツパス設定 - Tailwindがスキャンするファイルの場所
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',  // ページコンポーネント
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',  // 共通コンポーネント
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',  // App Routerコンポーネント
  ],
  
  // テーマ設定
  theme: {
    extend: {
      // カスタムカラーパレット定義
      colors: {
        // プライマリカラー（青系）
        primary: {
          50: '#eff6ff',   // 最も薄い青
          500: '#3b82f6',  // 標準の青
          600: '#2563eb',  // 濃い青
          700: '#1d4ed8',  // 最も濃い青
        },
        // セカンダリカラー（グレー系）
        secondary: {
          50: '#f8fafc',   // 最も薄いグレー
          500: '#64748b',  // 標準のグレー
          600: '#475569',  // 濃いグレー
          700: '#334155',  // 最も濃いグレー
        },
      },
    },
  },
  
  // プラグイン設定（現在は未使用）
  plugins: [],
}; 