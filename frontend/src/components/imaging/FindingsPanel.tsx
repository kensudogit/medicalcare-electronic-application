import type { ClassificationResult, ImageAnalysisResult, PhysicianReview } from '../../lib/api'
import { MODALITY_LABELS, PROVIDER_LABELS, parseClassifications } from '../../lib/api'

interface FindingsPanelProps {
  analysis: ImageAnalysisResult | null
  review?: PhysicianReview | null
  disclaimer?: string
}

/**
 * AI所見パネル — 確定診断としては表示しない
 */
export default function FindingsPanel({ analysis, review, disclaimer }: FindingsPanelProps) {
  if (!analysis) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-5 text-slate-500">
        解析結果がありません。画像をアップロードして解析を実行してください。
      </div>
    )
  }

  const classifications: ClassificationResult[] = parseClassifications(
    analysis.classificationsJson
  )

  return (
    <div className="space-y-4 rounded-lg border-2 border-amber-300 bg-amber-50/40 p-5">
      <div className="rounded-md bg-amber-100 px-3 py-2 text-sm font-semibold text-amber-950">
        AI診断支援候補（確定診断ではありません）
      </div>

      <div className="flex flex-wrap items-center gap-2 text-sm text-slate-600">
        <span className="rounded bg-teal-100 px-2 py-0.5 font-medium text-teal-800">
          {PROVIDER_LABELS[analysis.provider] || analysis.provider}
        </span>
        {analysis.modality && (
          <span className="rounded bg-slate-100 px-2 py-0.5">
            {MODALITY_LABELS[analysis.modality] || analysis.modality}
          </span>
        )}
        <span className="rounded bg-slate-800 px-2 py-0.5 text-xs text-white">
          {analysis.reviewStatus || 'PENDING_REVIEW'}
        </span>
        {analysis.isDiagnosticClaim === false && (
          <span className="rounded bg-red-700 px-2 py-0.5 text-xs text-white">
            NOT A DIAGNOSIS
          </span>
        )}
      </div>

      <div>
        <h3 className="mb-2 text-sm font-semibold text-slate-800">AI候補所見（参考）</h3>
        <p className="whitespace-pre-wrap rounded-md border border-amber-200 bg-white p-4 text-sm leading-relaxed text-slate-700">
          {analysis.findingsText || '（所見なし）'}
        </p>
        <p className="mt-2 text-xs font-medium text-amber-900">
          {disclaimer ||
            analysis.disclaimerText ||
            '※ 本表示はAIによる診断支援候補であり、確定診断ではありません。最終判断は必ず医師が行ってください。'}
        </p>
      </div>

      {classifications.length > 0 && (
        <div>
          <h3 className="mb-2 text-sm font-semibold text-slate-800">画像分類（候補）</h3>
          <ul className="space-y-2">
            {classifications.map((c, i) => (
              <li
                key={`${c.label}-${i}`}
                className="flex items-center justify-between rounded-md border border-slate-100 bg-white px-3 py-2 text-sm"
              >
                <span>
                  {c.label}
                  {c.category && (
                    <span className="ml-2 text-xs text-slate-400">{c.category}</span>
                  )}
                </span>
                <span className="font-mono text-teal-700">
                  信頼度 {(c.confidence * 100).toFixed(1)}%
                </span>
              </li>
            ))}
          </ul>
          <p className="mt-2 text-xs text-slate-500">
            信頼度が高くても誤検出の可能性があります。低い場合も見逃しを否定できません。
          </p>
        </div>
      )}

      {review && (
        <div className="rounded-md border border-teal-200 bg-white p-3 text-sm">
          <p className="font-semibold text-teal-900">医師確認: {review.decision}</p>
          {review.physicianComment && (
            <p className="mt-1 text-slate-700">{review.physicianComment}</p>
          )}
          {review.falsePositiveNotes && (
            <p className="mt-1 text-red-700">誤検出メモ: {review.falsePositiveNotes}</p>
          )}
          {review.missedFindingNotes && (
            <p className="mt-1 text-orange-800">見逃しメモ: {review.missedFindingNotes}</p>
          )}
        </div>
      )}

      {analysis.errorMessage && (
        <div className="rounded-md bg-red-50 p-3 text-sm text-red-700">
          {analysis.errorMessage}
        </div>
      )}
    </div>
  )
}
