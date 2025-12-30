import { useState, useEffect } from 'react'
import Head from 'next/head'

interface DeployStatus {
  status: string
  timestamp: string
  environment: string
  version: string
  buildTime: string
  deploymentUrl?: string
}

export default function DeployStatus() {
  const [deployStatus, setDeployStatus] = useState<DeployStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchDeployStatus = async () => {
      try {
        const response = await fetch('/api/vercel-deploy')
        if (response.ok) {
          const data = await response.json()
          setDeployStatus(data)
        } else {
          setError('デプロイ状態の取得に失敗しました')
        }
      } catch (err) {
        setError('ネットワークエラーが発生しました')
      } finally {
        setLoading(false)
      }
    }

    fetchDeployStatus()
  }, [])

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">デプロイ状態を確認中...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          <strong className="font-bold">エラー: </strong>
          <span className="block sm:inline">{error}</span>
        </div>
      </div>
    )
  }

  return (
    <>
      <Head>
        <title>デプロイ状態 - Medical Care Electronic Application</title>
        <meta name="description" content="Vercelデプロイ状態の確認" />
      </Head>

      <div className="min-h-screen bg-gray-100 py-12">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white shadow-lg rounded-lg overflow-hidden">
            <div className="px-6 py-8">
              <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">
                デプロイ状態
              </h1>

              {deployStatus && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-green-50 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-green-800">ステータス</h3>
                      <p className="text-green-600">{deployStatus.status}</p>
                    </div>

                    <div className="bg-blue-50 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-blue-800">環境</h3>
                      <p className="text-blue-600">{deployStatus.environment}</p>
                    </div>

                    <div className="bg-purple-50 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-purple-800">バージョン</h3>
                      <p className="text-purple-600">{deployStatus.version}</p>
                    </div>

                    <div className="bg-yellow-50 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-yellow-800">ビルド時刻</h3>
                      <p className="text-yellow-600">
                        {new Date(deployStatus.buildTime).toLocaleString('ja-JP')}
                      </p>
                    </div>
                  </div>

                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h3 className="text-lg font-semibold text-gray-800 mb-2">タイムスタンプ</h3>
                    <p className="text-gray-600">
                      {new Date(deployStatus.timestamp).toLocaleString('ja-JP')}
                    </p>
                  </div>

                  {deployStatus.deploymentUrl && (
                    <div className="bg-indigo-50 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-indigo-800 mb-2">デプロイURL</h3>
                      <a
                        href={deployStatus.deploymentUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-indigo-600 hover:text-indigo-800 underline break-all"
                      >
                        {deployStatus.deploymentUrl}
                      </a>
                    </div>
                  )}
                </div>
              )}

              <div className="mt-8 text-center">
                <a
                  href="/"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  ホームに戻る
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
