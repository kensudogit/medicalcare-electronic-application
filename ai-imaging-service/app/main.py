"""医療画像AI認識サービス (FastAPI)"""

import shutil
import uuid
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from app.config import settings
from app.models.schemas import (
    AiProvider,
    AnalyzeRequest,
    DetectionResult,
    DicomMetadata,
    HealthResponse,
    Modality,
)
from app.services.dicom_service import (
    detect_modality_from_filename,
    dicom_to_preview_png,
    extract_dicom_metadata,
    image_to_preview,
    is_dicom_file,
    modality_from_dicom,
)
from app.services.anonymization import anonymize_dicom_file, redact_metadata_dict
from app.services.provider_router import provider_router

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description=(
        "医療画像認識AIサービス。"
        "X線/CT/MRI/超音波/内視鏡/病理/DICOMに対応。"
        "自社モデル・SageMaker・Azure AI・Google Cloud・外部医療AI APIを切替可能。"
    ),
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

UPLOAD_DIR = Path(settings.upload_dir)
PREVIEW_DIR = Path(settings.preview_dir)
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
PREVIEW_DIR.mkdir(parents=True, exist_ok=True)

app.mount("/previews", StaticFiles(directory=str(PREVIEW_DIR)), name="previews")


@app.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    providers = await provider_router.availability_map()
    return HealthResponse(
        status="ok",
        version=settings.app_version,
        providers=providers,
        default_provider=settings.default_provider,
    )


@app.get("/providers")
async def list_providers():
    availability = await provider_router.availability_map()
    return {
        "default": settings.default_provider,
        "providers": [
            {
                "id": p.value,
                "name": _provider_label(p),
                "available": availability.get(p.value, False),
            }
            for p in AiProvider
        ],
        "modalities": [m.value for m in Modality],
    }


@app.post("/dicom/metadata", response_model=DicomMetadata)
async def dicom_metadata(file: UploadFile = File(...)):
    path = await _save_upload(file)
    try:
        if not is_dicom_file(path):
            raise HTTPException(status_code=400, detail="DICOMファイルではありません")
        meta = extract_dicom_metadata(path)
        # レスポンスからPHIを除去
        redacted = redact_metadata_dict(meta.model_dump())
        return DicomMetadata(**redacted)
    finally:
        path.unlink(missing_ok=True)


@app.post("/dicom/anonymize")
async def dicom_anonymize(file: UploadFile = File(...)):
    """DICOMファイルのPHI匿名化（保存用）"""
    path = await _save_upload(file)
    try:
        if not is_dicom_file(path):
            raise HTTPException(status_code=400, detail="DICOMファイルではありません")
        out_id = f"{uuid.uuid4().hex}_anon.dcm"
        out_path = UPLOAD_DIR / out_id
        anonymize_dicom_file(path, out_path)
        meta = extract_dicom_metadata(out_path)
        return {
            "message": "DICOMを匿名化しました",
            "anonymized_file": out_id,
            "metadata": redact_metadata_dict(meta.model_dump()),
        }
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"匿名化失敗: {exc}") from exc
    finally:
        path.unlink(missing_ok=True)


@app.post("/dicom/preview")
async def dicom_preview(file: UploadFile = File(...)):
    path = await _save_upload(file)
    try:
        if not is_dicom_file(path):
            raise HTTPException(status_code=400, detail="DICOMファイルではありません")
        preview_id = f"{uuid.uuid4().hex}.png"
        preview_path = PREVIEW_DIR / preview_id
        dicom_to_preview_png(path, preview_path)
        meta = extract_dicom_metadata(path)
        return {
            "preview_url": f"/previews/{preview_id}",
            "preview_id": preview_id,
            "metadata": meta,
            "modality": modality_from_dicom(meta).value,
        }
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"DICOMプレビュー生成失敗: {exc}") from exc
    finally:
        path.unlink(missing_ok=True)


@app.post("/analyze", response_model=DetectionResult)
async def analyze_image(
    file: UploadFile = File(...),
    modality: Optional[str] = Form(None),
    provider: Optional[str] = Form(None),
    generate_findings: bool = Form(True),
    patient_context: Optional[str] = Form(None),
):
    path = await _save_upload(file)
    preview_id = f"{uuid.uuid4().hex}.png"
    preview_path = PREVIEW_DIR / preview_id

    try:
        resolved_modality = _resolve_modality(path, file.filename or "", modality)
        resolved_provider = AiProvider(provider) if provider else None

        if is_dicom_file(path):
            try:
                dicom_to_preview_png(path, preview_path)
            except Exception:
                image_to_preview(path, preview_path) if path.suffix.lower() in {
                    ".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff", ".webp"
                } else None
        else:
            image_to_preview(path, preview_path)

        request = AnalyzeRequest(
            modality=resolved_modality,
            provider=resolved_provider,
            generate_findings=generate_findings,
            patient_context=patient_context,
        )
        result = await provider_router.analyze(
            path, resolved_modality, request, preview_path
        )
        # プレビューIDを raw に付与（呼び出し元で利用）
        raw = dict(result.raw or {})
        raw["preview_id"] = preview_id if preview_path.exists() else None
        raw["preview_url"] = f"/previews/{preview_id}" if preview_path.exists() else None
        result.raw = raw
        return result
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"解析失敗: {exc}") from exc
    finally:
        path.unlink(missing_ok=True)


@app.get("/previews/{preview_id}")
async def get_preview(preview_id: str):
    path = PREVIEW_DIR / preview_id
    if not path.exists():
        raise HTTPException(status_code=404, detail="プレビューが見つかりません")
    return FileResponse(path, media_type="image/png")


async def _save_upload(file: UploadFile) -> Path:
    suffix = Path(file.filename or "upload.bin").suffix or ".bin"
    dest = UPLOAD_DIR / f"{uuid.uuid4().hex}{suffix}"
    with dest.open("wb") as out:
        shutil.copyfileobj(file.file, out)
    return dest


def _resolve_modality(path: Path, filename: str, modality: Optional[str]) -> Modality:
    if modality:
        return Modality(modality.upper())
    if is_dicom_file(path):
        meta = extract_dicom_metadata(path)
        return modality_from_dicom(meta)
    return detect_modality_from_filename(filename)


def _provider_label(provider: AiProvider) -> str:
    labels = {
        AiProvider.INHOUSE: "自社AIモデル",
        AiProvider.SAGEMAKER: "AWS SageMaker",
        AiProvider.AZURE: "Azure AI",
        AiProvider.GOOGLE: "Google Cloud",
        AiProvider.EXTERNAL: "外部医療AI API",
    }
    return labels[provider]
