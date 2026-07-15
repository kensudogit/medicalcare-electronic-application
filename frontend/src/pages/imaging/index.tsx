import Head from 'next/head'
import Link from 'next/link'
import { useEffect, useMemo, useState } from 'react'
import DetectionOverlay from '../../components/imaging/DetectionOverlay'
import FindingsPanel from '../../components/imaging/FindingsPanel'
import ImageUploader from '../../components/imaging/ImageUploader'
import PhysicianApprovalPanel from '../../components/imaging/PhysicianApprovalPanel'
import ProviderSelector from '../../components/imaging/ProviderSelector'
import SafetyBanner from '../../components/imaging/SafetyBanner'
import {
  analyzeImage,
  fetchDisclaimer,
  fetchProviders,
  getImage,
  listImages,
  MODALITY_LABELS,
  parseBoxes,
  previewUrl,
  syncEhr,
  syncPacs,
  uploadImage,
  type AuthContext,
  type ImageAnalysisResult,
  type MedicalImage,
  type PhysicianReview,
} from '../../lib/api'

export default function ImagingPage() {
  const [images, setImages] = useState<MedicalImage[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [selectedImage, setSelectedImage] = useState<MedicalImage | null>(null)
  const [analysis, setAnalysis] = useState<ImageAnalysisResult | null>(null)
  const [review, setReview] = useState<PhysicianReview | null>(null)
  const [disclaimer, setDisclaimer] = useState('')
  const [provider, setProvider] = useState('inhouse')
  const [modality, setModality] = useState('XRAY')
  const [patientId, setPatientId] = useState('')
  const [patientName, setPatientName] = useState('')
  const [patientContext, setPatientContext] = useState('')
  const [userId, setUserId] = useState('1')
  const [userRole, setUserRole] = useState('PHYSICIAN')
  const [providerMeta, setProviderMeta] = useState<{
    providers: Array<{ id: string; name: string; available: boolean }>
    modalities: string[]
  }>({ providers: [], modalities: [] })
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [showBoxes, setShowBoxes] = useState(true)

  const auth: AuthContext = useMemo(
    () => ({ userId, userRole }),
    [userId, userRole]
  )

  const boxes = useMemo(
    () => (showBoxes ? parseBoxes(analysis?.detectionsJson) : []),
    [analysis, showBoxes]
  )

  const loadList = async () => {
    try {
      const data = await listImages(undefined, auth)
      setImages(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : '一覧取得エラー')
    }
  }

  const loadDetail = async (id: number) => {
    try {
      const data = await getImage(id, auth)
      setSelectedId(id)
      setSelectedImage(data.image)
      setAnalysis(data.latestAnalysis)
      setReview(data.reviews?.[0] || null)
      if (data.disclaimer) setDisclaimer(data.disclaimer)
      if (data.image.modality) setModality(data.image.modality)
    } catch (e) {
      setError(e instanceof Error ? e.message : '詳細取得エラー')
    }
  }

  useEffect(() => {
    fetchDisclaimer()
      .then((d) => setDisclaimer(d.disclaimer))
      .catch(() => {})
    loadList()
    fetchProviders(auth)
      .then((data) => {
        setProviderMeta({
          providers: data.providers || [],
          modalities: data.modalities || [],
        })
        if (data.default) setProvider(data.default)
      })
      .catch(() => {})
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleUpload = async () => {
    if (!selectedFile) {
      setError('ファイルを選択してください')
      return
    }
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const res = await uploadImage({
        file: selectedFile,
        modality,
        patientId: patientId || undefined,
        patientName: patientName || undefined,
        auth,
      })
      setMessage(res.message)
      setSelectedFile(null)
      setPatientId('')
      setPatientName('')
      await loadList()
      await loadDetail(res.image.id)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'アップロード失敗')
    } finally {
      setBusy(false)
    }
  }

  const handleAnalyze = async () => {
    if (!selectedId) {
      setError('解析する画像を選択してください')
      return
    }
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const res = await analyzeImage({
        id: selectedId,
        provider,
        generateFindings: true,
        patientContext: patientContext || undefined,
        auth,
      })
      setMessage(res.message)
      setAnalysis(res.result)
      if (res.disclaimer) setDisclaimer(res.disclaimer)
      await loadDetail(selectedId)
    } catch (e) {
      setError(e instanceof Error ? e.message : '解析失敗')
    } finally {
      setBusy(false)
    }
  }

  const handlePacs = async () => {
    if (!selectedId) return
    setBusy(true)
    try {
      const res = await syncPacs(selectedId, auth)
      setMessage(`PACS: ${res.message}（${res.accessionNumber}）`)
      await loadDetail(selectedId)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'PACS連携失敗')
    } finally {
      setBusy(false)
    }
  }

  const handleEhr = async () => {
    if (!selectedId) return
    setBusy(true)
    try {
      const res = await syncEhr(selectedId, analysis?.id, auth)
      setMessage(`電子カルテ: ${res.message}（${res.documentId}）${res.warning ? ' / ' + res.warning : ''}`)
      await loadDetail(selectedId)
    } catch (e) {
      setError(e instanceof Error ? e.message : '電子カルテ連携失敗')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <Head>
        <title>医療画像AI認識（診断支援） | Medical Care</title>
      </Head>

      <main className="min-h-screen bg-gradient-to-br from-slate-100 via-teal-50 to-slate-200">
        <header className="border-b border-teal-900/10 bg-teal-950 text-white">
          <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-4">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-amber-200">
                Clinical Decision Support — Not a Diagnosis
              </p>
              <h1 className="text-2xl font-semibold">医療画像認識（診断支援）</h1>
            </div>
            <Link href="/" className="text-sm text-teal-100 hover:text-white">
              ← ホーム
            </Link>
          </div>
        </header>

        <div className="mx-auto max-w-7xl space-y-4 px-4 py-6">
          <SafetyBanner />

          <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
            <aside className="space-y-4">
              <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <h2 className="mb-3 text-sm font-semibold text-slate-800">アクセス権限（デモ）</h2>
                <div className="grid gap-2">
                  <label className="text-xs">
                    ユーザーID
                    <input
                      value={userId}
                      onChange={(e) => setUserId(e.target.value)}
                      className="mt-1 w-full rounded border border-slate-300 px-2 py-1.5 text-sm"
                    />
                  </label>
                  <label className="text-xs">
                    ロール
                    <select
                      value={userRole}
                      onChange={(e) => setUserRole(e.target.value)}
                      className="mt-1 w-full rounded border border-slate-300 px-2 py-1.5 text-sm"
                    >
                      <option value="ADMIN">ADMIN</option>
                      <option value="PHYSICIAN">PHYSICIAN</option>
                      <option value="RADIOLOGIST">RADIOLOGIST</option>
                      <option value="TECHNICIAN">TECHNICIAN</option>
                      <option value="INSTITUTION_ADMIN">INSTITUTION_ADMIN</option>
                      <option value="USER">USER</option>
                    </select>
                  </label>
                </div>
              </section>

              <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <h2 className="mb-3 text-sm font-semibold text-slate-800">画像アップロード</h2>
                <ImageUploader
                  disabled={busy}
                  onFileSelected={(file) => {
                    setSelectedFile(file)
                    setMessage(`選択: ${file.name}`)
                  }}
                />
                {selectedFile && (
                  <p className="mt-2 truncate text-xs text-slate-500">{selectedFile.name}</p>
                )}

                <div className="mt-4 space-y-3">
                  <ProviderSelector
                    provider={provider}
                    modality={modality}
                    providers={providerMeta.providers}
                    modalities={providerMeta.modalities}
                    onProviderChange={setProvider}
                    onModalityChange={setModality}
                  />
                  <label className="block text-sm">
                    <span className="mb-1 block font-medium text-slate-700">
                      患者ID（保存時に匿名化）
                    </span>
                    <input
                      value={patientId}
                      onChange={(e) => setPatientId(e.target.value)}
                      className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                      placeholder="平文は保存されません"
                      autoComplete="off"
                    />
                  </label>
                  <label className="block text-sm">
                    <span className="mb-1 block font-medium text-slate-700">
                      患者氏名（マスク／ハッシュ化）
                    </span>
                    <input
                      value={patientName}
                      onChange={(e) => setPatientName(e.target.value)}
                      className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                      placeholder="平文は保存されません"
                      autoComplete="off"
                    />
                  </label>
                  <label className="block text-sm">
                    <span className="mb-1 block font-medium text-slate-700">臨床情報（所見生成用）</span>
                    <textarea
                      value={patientContext}
                      onChange={(e) => setPatientContext(e.target.value)}
                      rows={2}
                      className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                      placeholder="例: 発熱・咳嗽あり"
                    />
                  </label>
                  <button
                    type="button"
                    disabled={busy || !selectedFile}
                    onClick={handleUpload}
                    className="w-full rounded-md bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800 disabled:opacity-50"
                  >
                    アップロード（匿名化・暗号化）
                  </button>
                  <button
                    type="button"
                    disabled={busy || !selectedId}
                    onClick={handleAnalyze}
                    className="w-full rounded-md bg-slate-800 px-4 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-50"
                  >
                    AI候補検出（確定診断ではない）
                  </button>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      disabled={busy || !selectedId}
                      onClick={handlePacs}
                      className="rounded-md border border-slate-300 px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                    >
                      PACS連携
                    </button>
                    <button
                      type="button"
                      disabled={busy || !selectedId}
                      onClick={handleEhr}
                      className="rounded-md border border-slate-300 px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                    >
                      電子カルテ連携
                    </button>
                  </div>
                </div>
              </section>

              <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <h2 className="mb-3 text-sm font-semibold text-slate-800">アップロード済み画像</h2>
                <ul className="max-h-80 space-y-2 overflow-y-auto">
                  {images.length === 0 && (
                    <li className="text-sm text-slate-400">まだ画像がありません</li>
                  )}
                  {images.map((img) => (
                    <li key={img.id}>
                      <button
                        type="button"
                        onClick={() => loadDetail(img.id)}
                        className={`w-full rounded-md px-3 py-2 text-left text-sm ${
                          selectedId === img.id
                            ? 'bg-teal-700 text-white'
                            : 'bg-slate-50 text-slate-700 hover:bg-slate-100'
                        }`}
                      >
                        <div className="truncate font-medium">{img.originalFileName}</div>
                        <div className="mt-0.5 text-xs opacity-80">
                          {MODALITY_LABELS[img.modality] || img.modality} · {img.status}
                          {img.patientAlias ? ` · ${img.patientAlias}` : ''}
                          {img.fileEncrypted ? ' · ENC' : ''}
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              </section>
            </aside>

            <section className="space-y-4">
              {(message || error) && (
                <div
                  className={`rounded-md px-4 py-3 text-sm ${
                    error ? 'bg-red-50 text-red-700' : 'bg-teal-50 text-teal-800'
                  }`}
                >
                  {error || message}
                </div>
              )}

              <SafetyBanner compact />

              <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                  <h2 className="text-sm font-semibold text-slate-800">
                    DICOM / 画像表示・検出枠（候補）
                  </h2>
                  <label className="flex items-center gap-2 text-xs text-slate-600">
                    <input
                      type="checkbox"
                      checked={showBoxes}
                      onChange={(e) => setShowBoxes(e.target.checked)}
                    />
                    検出枠を表示（オフにして原画像のみ確認推奨）
                  </label>
                </div>

                {selectedImage && (
                  <div className="mb-3 text-xs text-slate-500">
                    {selectedImage.isDicom ? 'DICOM' : '画像'} · ID {selectedImage.id}
                    {selectedImage.patientAlias && ` · ${selectedImage.patientAlias}`}
                    {selectedImage.patientNameMasked && ` · ${selectedImage.patientNameMasked}`}
                    {selectedImage.fileEncrypted && ' · 暗号化保存'}
                    {selectedImage.pacsAccessionNumber &&
                      ` · PACS ${selectedImage.pacsAccessionNumber}`}
                  </div>
                )}

                <p className="mb-3 text-xs text-amber-800">
                  検出枠は候補提示です。枠の有無にかかわらず原画像全体を確認し、見逃しを想定してください。
                </p>

                {selectedId ? (
                  <DetectionOverlay
                    imageUrl={previewUrl(selectedId)}
                    boxes={boxes}
                    alt={selectedImage?.originalFileName || 'preview'}
                  />
                ) : (
                  <div className="flex h-64 items-center justify-center rounded-lg bg-slate-100 text-sm text-slate-400">
                    画像を選択するとプレビューと検出枠が表示されます
                  </div>
                )}

                {boxes.length > 0 && (
                  <ul className="mt-4 grid gap-2 sm:grid-cols-2">
                    {boxes.map((b, i) => (
                      <li
                        key={`${b.label}-${i}`}
                        className="rounded-md border border-amber-100 bg-amber-50/50 px-3 py-2 text-sm"
                      >
                        <span className="font-medium">{b.label}</span>
                        <span className="ml-2 text-teal-700">
                          {(b.confidence * 100).toFixed(0)}%
                        </span>
                        <span className="ml-2 text-xs text-amber-800">候補</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <FindingsPanel analysis={analysis} review={review} disclaimer={disclaimer} />

              <PhysicianApprovalPanel
                analysis={analysis}
                imageId={selectedId}
                userId={userId}
                userRole={userRole}
                onSubmitted={(r) => {
                  setReview(r)
                  setMessage('医師確認を記録しました（確定診断ではありません）')
                  if (selectedId) loadDetail(selectedId)
                }}
              />
            </section>
          </div>
        </div>
      </main>
    </>
  )
}
