import { useRef, useState } from 'react'

interface ImageUploaderProps {
  onFileSelected: (file: File) => void
  accept?: string
  disabled?: boolean
}

export default function ImageUploader({
  onFileSelected,
  accept = '.jpg,.jpeg,.png,.bmp,.tif,.tiff,.webp,.dcm,.dicom,image/*,application/dicom',
  disabled,
}: ImageUploaderProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [dragOver, setDragOver] = useState(false)

  const handleFiles = (files: FileList | null) => {
    if (!files || files.length === 0) return
    onFileSelected(files[0])
  }

  return (
    <div
      className={`rounded-xl border-2 border-dashed p-8 text-center transition-colors ${
        dragOver ? 'border-teal-500 bg-teal-50' : 'border-slate-300 bg-slate-50'
      } ${disabled ? 'opacity-50 pointer-events-none' : 'cursor-pointer'}`}
      onClick={() => inputRef.current?.click()}
      onDragOver={(e) => {
        e.preventDefault()
        setDragOver(true)
      }}
      onDragLeave={() => setDragOver(false)}
      onDrop={(e) => {
        e.preventDefault()
        setDragOver(false)
        handleFiles(e.dataTransfer.files)
      }}
    >
      <input
        ref={inputRef}
        type="file"
        className="hidden"
        accept={accept}
        disabled={disabled}
        onChange={(e) => handleFiles(e.target.files)}
      />
      <p className="text-lg font-medium text-slate-800">画像をドラッグ＆ドロップ</p>
      <p className="mt-2 text-sm text-slate-500">
        X線 / CT / MRI / 超音波 / 内視鏡 / 病理 / DICOM（.dcm）
      </p>
      <button
        type="button"
        className="mt-4 rounded-md bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800"
      >
        ファイルを選択
      </button>
    </div>
  )
}
