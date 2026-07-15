import { useState } from 'react'
import type { ImageAnalysisResult, PhysicianReview } from '../../lib/api'
import { submitPhysicianReview } from '../../lib/api'

interface PhysicianApprovalPanelProps {
  analysis: ImageAnalysisResult | null
  imageId: number | null
  userId: string
  userRole: string
  onSubmitted: (review: PhysicianReview) => void
}

const DECISIONS = [
  { value: 'APPROVED_AS_CANDIDATE', label: '候補として妥当（確定診断ではない）' },
  { value: 'REJECTED', label: '却下（臨床的に採用しない）' },
  { value: 'FALSE_POSITIVE', label: '誤検出あり' },
  { value: 'MISSED_FINDING', label: '見逃し・追加所見あり' },
  { value: 'NEEDS_SECOND_LOOK', label: '要セカンドオピニオン' },
]

/**
 * 医師による確認と承認パネル
 */
export default function PhysicianApprovalPanel({
  analysis,
  imageId,
  userId,
  userRole,
  onSubmitted,
}: PhysicianApprovalPanelProps) {
  const [decision, setDecision] = useState('APPROVED_AS_CANDIDATE')
  const [comment, setComment] = useState('')
  const [fpNotes, setFpNotes] = useState('')
  const [missNotes, setMissNotes] = useState('')
  const [ack, setAck] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const canApprove = ['ADMIN', 'PHYSICIAN', 'RADIOLOGIST'].includes(userRole.toUpperCase())

  if (!analysis || !imageId) {
    return null
  }

  if (!canApprove) {
    return (
      <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
        医師承認には PHYSICIAN / RADIOLOGIST / ADMIN ロールが必要です（現在: {userRole}）。
      </div>
    )
  }

  const handleSubmit = async () => {
    setBusy(true)
    setError(null)
    try {
      const res = await submitPhysicianReview({
        imageId,
        analysisResultId: analysis.id,
        decision,
        physicianComment: comment,
        falsePositiveNotes: fpNotes || undefined,
        missedFindingNotes: missNotes || undefined,
        acknowledgedNonDiagnostic: ack,
        userId,
        userRole,
      })
      onSubmitted(res.review)
    } catch (e) {
      setError(e instanceof Error ? e.message : '承認送信に失敗しました')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="space-y-3 rounded-xl border border-teal-700/30 bg-white p-4 shadow-sm">
      <h2 className="text-sm font-semibold text-teal-900">医師による確認・承認</h2>
      <p className="text-xs text-slate-600">
        AI候補を確認し、誤検出・見逃しの有無を記録してください。承認しても「確定診断」にはなりません。
      </p>

      <label className="block text-sm">
        <span className="mb-1 block font-medium">判定</span>
        <select
          value={decision}
          onChange={(e) => setDecision(e.target.value)}
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          {DECISIONS.map((d) => (
            <option key={d.value} value={d.value}>
              {d.label}
            </option>
          ))}
        </select>
      </label>

      <label className="block text-sm">
        <span className="mb-1 block font-medium">医師コメント</span>
        <textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          rows={2}
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          placeholder="確認内容（診断名の確定記録は電子カルテ側で実施）"
        />
      </label>

      <div className="grid gap-3 sm:grid-cols-2">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-red-800">誤検出メモ</span>
          <textarea
            value={fpNotes}
            onChange={(e) => setFpNotes(e.target.value)}
            rows={2}
            className="w-full rounded-md border border-red-200 px-3 py-2 text-sm"
            placeholder="AIが誤って指摘した領域など"
          />
        </label>
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-orange-900">見逃しメモ</span>
          <textarea
            value={missNotes}
            onChange={(e) => setMissNotes(e.target.value)}
            rows={2}
            className="w-full rounded-md border border-orange-200 px-3 py-2 text-sm"
            placeholder="AIが指摘しなかった所見など"
          />
        </label>
      </div>

      <label className="flex items-start gap-2 text-sm text-slate-800">
        <input
          type="checkbox"
          checked={ack}
          onChange={(e) => setAck(e.target.checked)}
          className="mt-1"
        />
        <span>
          AI結果は<strong>確定診断ではない</strong>ことを理解し、最終判断は医師が行うことに同意します（必須）。
        </span>
      </label>

      {error && <p className="text-sm text-red-600">{error}</p>}

      <button
        type="button"
        disabled={busy || !ack}
        onClick={handleSubmit}
        className="rounded-md bg-teal-800 px-4 py-2 text-sm font-medium text-white hover:bg-teal-900 disabled:opacity-50"
      >
        医師確認を記録
      </button>
    </section>
  )
}
