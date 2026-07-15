interface SafetyBannerProps {
  compact?: boolean
}

/**
 * 誤検出・見逃しを前提とした安全設計バナー
 */
export default function SafetyBanner({ compact }: SafetyBannerProps) {
  if (compact) {
    return (
      <div className="rounded-md border border-amber-400 bg-amber-50 px-3 py-2 text-xs text-amber-950">
        AI候補には<strong>誤検出・見逃し</strong>があり得ます。確定診断として使用しないでください。
      </div>
    )
  }

  return (
    <section className="rounded-xl border-2 border-amber-500 bg-gradient-to-r from-amber-50 to-orange-50 p-4 shadow-sm">
      <h2 className="text-base font-bold text-amber-950">重要な安全上の注意</h2>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-amber-950">
        <li>
          本画面のAI出力は<strong>診断支援候補</strong>であり、<strong>確定診断ではありません</strong>。
        </li>
        <li>
          <strong>誤検出（False Positive）</strong>と<strong>見逃し（False Negative）</strong>を前提に、
          必ず医師が原画像を確認してください。
        </li>
        <li>
          検出枠が無い／信頼度が低い場合でも、異常がないことを意味しません。
        </li>
        <li>
          電子カルテへの診断記録は医師が作成し、AI文言を診断名として転記しないでください。
        </li>
        <li>
          患者氏名・患者IDは匿名化して表示・保存されます。
        </li>
      </ul>
    </section>
  )
}
