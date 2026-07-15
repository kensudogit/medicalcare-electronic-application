import Head from 'next/head'
import Link from 'next/link'

export default function Home() {
  return (
    <>
      <Head>
        <title>Medical Care Electronic Application</title>
        <meta name="description" content="医療電子申請システム" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <main className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="container mx-auto px-4 py-16">
          <div className="text-center">
            <h1 className="text-5xl font-bold text-gray-900 mb-6">
              Medical Care Electronic Application
            </h1>
            <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
              医療電子申請システムへようこそ。安全で効率的な医療申請プロセスを提供します。
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center mb-12">
              <Link
                href="/imaging"
                className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-teal-700 hover:bg-teal-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 transition-colors"
              >
                医療画像AI認識
              </Link>
              <Link
                href="/deploy-status"
                className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
              >
                デプロイ状態確認
              </Link>
              <Link
                href="/api/health"
                className="inline-flex items-center px-6 py-3 border border-gray-300 text-base font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors"
              >
                ヘルスチェック
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto">
              <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                  <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">安全な申請</h3>
                <p className="text-gray-600">暗号化された安全な通信で医療申請を行います</p>
              </div>

              <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                  <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">高速処理</h3>
                <p className="text-gray-600">最新技術による高速で効率的な申請処理</p>
              </div>

              <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                  <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">プライバシー保護</h3>
                <p className="text-gray-600">厳格なプライバシー保護とデータセキュリティ</p>
              </div>
            </div>

            <div className="mt-12 p-6 bg-white rounded-lg shadow-md max-w-2xl mx-auto">
              <h2 className="text-2xl font-bold text-gray-900 mb-4">システム情報</h2>
              <div className="text-left space-y-2">
                <p><span className="font-semibold">環境:</span> {process.env.NODE_ENV || 'development'}</p>
                <p><span className="font-semibold">バージョン:</span> {process.env.NEXT_PUBLIC_APP_VERSION || '1.0.0'}</p>
                <p><span className="font-semibold">フレームワーク:</span> Next.js 14</p>
                <p><span className="font-semibold">スタイリング:</span> Tailwind CSS</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </>
  )
} 