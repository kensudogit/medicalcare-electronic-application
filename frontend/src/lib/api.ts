/**
 * Spring Boot API クライアント（医療画像・コンプライアンス対応）
 */

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8081/api'

export type Modality =
  | 'XRAY'
  | 'CT'
  | 'MRI'
  | 'ULTRASOUND'
  | 'ENDOSCOPY'
  | 'PATHOLOGY'
  | 'DICOM'
  | 'OTHER'

export type AiProvider = 'inhouse' | 'sagemaker' | 'azure' | 'google' | 'external'

export interface AuthContext {
  userId?: string
  userRole?: string
  institutionId?: string
}

export interface BoundingBox {
  x: number
  y: number
  width: number
  height: number
  label: string
  confidence: number
  finding_code?: string
}

export interface ClassificationResult {
  label: string
  confidence: number
  category?: string
}

export interface MedicalImage {
  id: number
  studyUid?: string
  seriesUid?: string
  sopInstanceUid?: string
  patientId?: string | null
  patientAlias?: string
  patientNameMasked?: string
  modality: Modality | string
  originalFileName: string
  storedFileName: string
  filePath: string
  previewPath?: string
  contentType?: string
  fileSize?: number
  isDicom?: boolean
  dicomMetadata?: string
  fileEncrypted?: boolean
  phiEncrypted?: boolean
  status: string
  medicalInstitutionId?: number
  applicationId?: number
  uploadedByUserId?: number
  pacsAccessionNumber?: string
  ehrDocumentId?: string
  createdAt?: string
  updatedAt?: string
}

export interface ImageAnalysisResult {
  id: number
  medicalImageId: number
  provider: string
  modelVersion?: string
  modality?: string
  detectionsJson?: string
  classificationsJson?: string
  findingsText?: string
  rawResponse?: string
  processingMs?: number
  status: string
  errorMessage?: string
  reviewStatus?: string
  isDiagnosticClaim?: boolean
  disclaimerText?: string
  createdAt?: string
}

export interface PhysicianReview {
  id: number
  medicalImageId: number
  analysisResultId: number
  reviewerUserId: number
  reviewerRole?: string
  decision: string
  physicianComment?: string
  confirmedFindingsText?: string
  falsePositiveNotes?: string
  missedFindingNotes?: string
  acknowledgedNonDiagnostic?: boolean
  reviewedAt?: string
  createdAt?: string
}

function imagingUrl(path: string): string {
  return `${API_BASE}/api/medical-images${path}`
}

function authHeaders(auth?: AuthContext): HeadersInit {
  const h: Record<string, string> = {}
  if (auth?.userId) h['X-User-Id'] = auth.userId
  if (auth?.userRole) h['X-User-Role'] = auth.userRole
  if (auth?.institutionId) h['X-Institution-Id'] = auth.institutionId
  return h
}

export async function fetchDisclaimer() {
  const res = await fetch(imagingUrl('/compliance/disclaimer'))
  if (!res.ok) throw new Error('免責情報の取得に失敗しました')
  return res.json() as Promise<{
    disclaimer: string
    isDiagnosticClaimAllowed: boolean
    requiresPhysicianReview: boolean
    assumesFalsePositivesAndMisses: boolean
  }>
}

export async function fetchProviders(auth?: AuthContext) {
  const res = await fetch(imagingUrl('/providers'), { headers: authHeaders(auth) })
  if (!res.ok) throw new Error('プロバイダー一覧の取得に失敗しました')
  return res.json()
}

export async function listImages(institutionId?: number, auth?: AuthContext): Promise<MedicalImage[]> {
  const q = institutionId ? `?institutionId=${institutionId}` : ''
  const res = await fetch(imagingUrl(q), { headers: authHeaders(auth) })
  if (!res.ok) throw new Error('画像一覧の取得に失敗しました')
  return res.json()
}

export async function getImage(id: number, auth?: AuthContext) {
  const res = await fetch(imagingUrl(`/${id}`), { headers: authHeaders(auth) })
  if (!res.ok) throw new Error('画像詳細の取得に失敗しました')
  return res.json() as Promise<{
    image: MedicalImage
    latestAnalysis: ImageAnalysisResult | null
    analyses: ImageAnalysisResult[]
    reviews: PhysicianReview[]
    disclaimer: string
    isDiagnosticClaimAllowed: boolean
  }>
}

export async function uploadImage(params: {
  file: File
  modality?: Modality | string
  patientId?: string
  patientName?: string
  medicalInstitutionId?: number
  applicationId?: number
  auth?: AuthContext
}): Promise<{ message: string; image: MedicalImage; disclaimer?: string }> {
  const form = new FormData()
  form.append('file', params.file)
  if (params.modality) form.append('modality', params.modality)
  if (params.patientId) form.append('patientId', params.patientId)
  if (params.patientName) form.append('patientName', params.patientName)
  if (params.medicalInstitutionId != null) {
    form.append('medicalInstitutionId', String(params.medicalInstitutionId))
  }
  if (params.applicationId != null) {
    form.append('applicationId', String(params.applicationId))
  }
  const res = await fetch(imagingUrl('/upload'), {
    method: 'POST',
    headers: authHeaders(params.auth),
    body: form,
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || 'アップロードに失敗しました')
  return data
}

export async function analyzeImage(params: {
  id: number
  provider?: AiProvider | string
  generateFindings?: boolean
  patientContext?: string
  auth?: AuthContext
}): Promise<{ message: string; result: ImageAnalysisResult; disclaimer?: string }> {
  const q = new URLSearchParams()
  if (params.provider) q.set('provider', params.provider)
  if (params.generateFindings != null) q.set('generateFindings', String(params.generateFindings))
  if (params.patientContext) q.set('patientContext', params.patientContext)
  const res = await fetch(imagingUrl(`/${params.id}/analyze?${q.toString()}`), {
    method: 'POST',
    headers: authHeaders(params.auth),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || '解析に失敗しました')
  return data
}

export async function submitPhysicianReview(params: {
  imageId: number
  analysisResultId: number
  decision: string
  physicianComment?: string
  confirmedFindingsText?: string
  falsePositiveNotes?: string
  missedFindingNotes?: string
  acknowledgedNonDiagnostic: boolean
  userId: string
  userRole: string
}): Promise<{ message: string; review: PhysicianReview }> {
  const res = await fetch(imagingUrl(`/${params.imageId}/reviews`), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders({ userId: params.userId, userRole: params.userRole }),
    },
    body: JSON.stringify({
      analysisResultId: params.analysisResultId,
      decision: params.decision,
      physicianComment: params.physicianComment,
      confirmedFindingsText: params.confirmedFindingsText,
      falsePositiveNotes: params.falsePositiveNotes,
      missedFindingNotes: params.missedFindingNotes,
      acknowledgedNonDiagnostic: params.acknowledgedNonDiagnostic,
    }),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || '医師確認の送信に失敗しました')
  return data
}

export async function syncPacs(id: number, auth?: AuthContext) {
  const res = await fetch(imagingUrl(`/${id}/sync-pacs`), {
    method: 'POST',
    headers: authHeaders(auth),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || 'PACS連携に失敗しました')
  return data
}

export async function syncEhr(id: number, analysisResultId?: number, auth?: AuthContext) {
  const q = analysisResultId ? `?analysisResultId=${analysisResultId}` : ''
  const res = await fetch(imagingUrl(`/${id}/sync-ehr${q}`), {
    method: 'POST',
    headers: authHeaders(auth),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || '電子カルテ連携に失敗しました')
  return data
}

export function previewUrl(id: number): string {
  return imagingUrl(`/${id}/preview`)
}

export function parseBoxes(json?: string): BoundingBox[] {
  if (!json) return []
  try {
    return JSON.parse(json) as BoundingBox[]
  } catch {
    return []
  }
}

export function parseClassifications(json?: string): ClassificationResult[] {
  if (!json) return []
  try {
    return JSON.parse(json) as ClassificationResult[]
  } catch {
    return []
  }
}

export const MODALITY_LABELS: Record<string, string> = {
  XRAY: 'X線',
  CT: 'CT',
  MRI: 'MRI',
  ULTRASOUND: '超音波',
  ENDOSCOPY: '内視鏡',
  PATHOLOGY: '病理画像',
  DICOM: 'DICOM',
  OTHER: 'その他',
}

export const PROVIDER_LABELS: Record<string, string> = {
  inhouse: '自社AIモデル',
  sagemaker: 'AWS SageMaker',
  azure: 'Azure AI',
  google: 'Google Cloud',
  external: '外部医療AI API',
}
