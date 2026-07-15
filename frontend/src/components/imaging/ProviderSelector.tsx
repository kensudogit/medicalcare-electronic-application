import { PROVIDER_LABELS, MODALITY_LABELS, type AiProvider, type Modality } from '../../lib/api'

interface ProviderSelectorProps {
  provider: string
  modality: string
  providers: Array<{ id: string; name: string; available: boolean }>
  modalities: string[]
  onProviderChange: (v: string) => void
  onModalityChange: (v: string) => void
}

export default function ProviderSelector({
  provider,
  modality,
  providers,
  modalities,
  onProviderChange,
  onModalityChange,
}: ProviderSelectorProps) {
  const providerOptions =
    providers.length > 0
      ? providers
      : (Object.keys(PROVIDER_LABELS) as AiProvider[]).map((id) => ({
          id,
          name: PROVIDER_LABELS[id],
          available: true,
        }))

  const modalityOptions =
    modalities.length > 0 ? modalities : (Object.keys(MODALITY_LABELS) as Modality[])

  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <label className="block text-sm">
        <span className="mb-1 block font-medium text-slate-700">画像認識サービス</span>
        <select
          value={provider}
          onChange={(e) => onProviderChange(e.target.value)}
          className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
        >
          {providerOptions.map((p) => (
            <option key={p.id} value={p.id} disabled={!p.available && providers.length > 0}>
              {p.name}
              {!p.available && providers.length > 0 ? '（利用不可）' : ''}
            </option>
          ))}
        </select>
      </label>

      <label className="block text-sm">
        <span className="mb-1 block font-medium text-slate-700">モダリティ</span>
        <select
          value={modality}
          onChange={(e) => onModalityChange(e.target.value)}
          className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
        >
          {modalityOptions.map((m) => (
            <option key={m} value={m}>
              {MODALITY_LABELS[m] || m}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
