import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';

// Interフォントの設定 - 読みやすいWebフォントを使用
const inter = Inter({ subsets: ['latin'] });

// メタデータの設定 - SEOとブラウザ表示用
export const metadata: Metadata = {
  title: '医療系電子申請システム',
  description: '保険医療機関等の申請・届出をオンラインで申請・届出することを可能にするシステム',
};

// ルートレイアウトコンポーネント - 全ページの共通レイアウトを定義
export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ja">
      <body className={inter.className}>
        {/* 最小高さを画面全体に設定し、グレー背景を適用 */}
        <div className="min-h-screen bg-gray-50">
          {children}
        </div>
      </body>
    </html>
  );
} 