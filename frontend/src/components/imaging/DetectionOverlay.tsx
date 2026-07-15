import { useEffect, useRef } from 'react'
import type { BoundingBox } from '../../lib/api'

interface DetectionOverlayProps {
  imageUrl: string
  boxes: BoundingBox[]
  alt?: string
  showLabels?: boolean
}

/**
 * 画像上に病変候補の検出枠を描画
 */
export default function DetectionOverlay({
  imageUrl,
  boxes,
  alt = '医療画像',
  showLabels = true,
}: DetectionOverlayProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const imgRef = useRef<HTMLImageElement>(null)

  useEffect(() => {
    const img = imgRef.current
    const canvas = canvasRef.current
    if (!img || !canvas) return

    const draw = () => {
      const w = img.clientWidth
      const h = img.clientHeight
      if (w === 0 || h === 0) return
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')
      if (!ctx) return
      ctx.clearRect(0, 0, w, h)

      boxes.forEach((box, index) => {
        const x = box.x * w
        const y = box.y * h
        const bw = box.width * w
        const bh = box.height * h
        const color = COLORS[index % COLORS.length]

        ctx.strokeStyle = color
        ctx.lineWidth = 2
        ctx.strokeRect(x, y, bw, bh)

        if (showLabels) {
          const label = `${box.label} ${(box.confidence * 100).toFixed(0)}%`
          ctx.font = '12px sans-serif'
          const textWidth = ctx.measureText(label).width
          ctx.fillStyle = color
          ctx.fillRect(x, Math.max(0, y - 18), textWidth + 8, 18)
          ctx.fillStyle = '#fff'
          ctx.fillText(label, x + 4, Math.max(12, y - 5))
        }
      })
    }

    if (img.complete) draw()
    img.addEventListener('load', draw)
    window.addEventListener('resize', draw)
    return () => {
      img.removeEventListener('load', draw)
      window.removeEventListener('resize', draw)
    }
  }, [imageUrl, boxes, showLabels])

  return (
    <div className="relative inline-block max-w-full overflow-hidden rounded-lg bg-black">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img
        ref={imgRef}
        src={imageUrl}
        alt={alt}
        className="block max-h-[70vh] max-w-full object-contain"
      />
      <canvas
        ref={canvasRef}
        className="pointer-events-none absolute left-0 top-0 h-full w-full"
      />
    </div>
  )
}

const COLORS = ['#14b8a6', '#f59e0b', '#ef4444', '#3b82f6', '#a855f7', '#22c55e']
