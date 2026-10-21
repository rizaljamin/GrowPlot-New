"""Authenticated YOLO-World plant-part detector for GrowPlot diagnosis."""
import hmac
import io
import os
from typing import Annotated

from fastapi import FastAPI, File, Header, HTTPException, UploadFile
from PIL import Image, ImageOps, UnidentifiedImageError
from ultralytics import YOLOWorld

MODEL_NAME = os.environ.get("YOLO_MODEL", "yolov8s-world.pt")
TOKEN = os.environ.get("GROWPLOT_YOLO_SERVICE_TOKEN", "")
MAX_FILE_BYTES = 10 * 1024 * 1024
MAX_PHOTOS = 5
CLASSES = ["plant", "leaf", "flower", "fruit"]

app = FastAPI(title="GrowPlot YOLO-World", docs_url=None, redoc_url=None)
model = None


@app.on_event("startup")
def load_model():
    global model
    model = YOLOWorld(MODEL_NAME)
    model.set_classes(CLASSES)


@app.get("/healthz")
def health():
    return {"ok": True, "model": MODEL_NAME, "loaded": model is not None}


@app.post("/v1/yolo")
async def detect(
    photos: Annotated[list[UploadFile], File()],
    authorization: Annotated[str | None, Header()] = None,
):
    if not TOKEN:
        raise HTTPException(status_code=503, detail="YOLO service access is not configured.")
    if not hmac.compare_digest(authorization or "", f"Bearer {TOKEN}"):
        raise HTTPException(status_code=401, detail="Unauthorized.")
    if not photos or len(photos) > MAX_PHOTOS:
        raise HTTPException(status_code=400, detail="Attach between one and five photos.")
    output = []
    for index, upload in enumerate(photos):
        if upload.content_type not in ("image/jpeg", "image/png"):
            raise HTTPException(status_code=415, detail="Photos must be JPEG or PNG.")
        raw = await upload.read(MAX_FILE_BYTES + 1)
        if not raw or len(raw) > MAX_FILE_BYTES:
            raise HTTPException(status_code=413, detail="Each photo must be 10 MB or smaller.")
        try:
            image = ImageOps.exif_transpose(Image.open(io.BytesIO(raw))).convert("RGB")
        except (UnidentifiedImageError, OSError):
            raise HTTPException(status_code=400, detail="A submitted photo could not be decoded.")
        width, height = image.size
        result = model.predict(image, conf=0.20, iou=0.55, imgsz=640, max_det=40, verbose=False)[0]
        detections = []
        if result.boxes is not None:
            for box in result.boxes:
                cls_id = int(box.cls.item())
                coords = box.xyxy[0].tolist()
                normalized = [
                    max(0, min(1000, round(coords[0] * 1000 / width))),
                    max(0, min(1000, round(coords[1] * 1000 / height))),
                    max(0, min(1000, round(coords[2] * 1000 / width))),
                    max(0, min(1000, round(coords[3] * 1000 / height))),
                ]
                detections.append({
                    "label": CLASSES[cls_id] if 0 <= cls_id < len(CLASSES) else "plant part",
                    "confidence": round(float(box.conf.item()), 4),
                    "box": normalized,
                })
        output.append({"imageIndex": index, "width": width, "height": height, "detections": detections})
    return {
        "provider": "YOLO-World",
        "status": "ok",
        "model": MODEL_NAME,
        "task": "open-vocabulary plant-part detection",
        "classes": CLASSES,
        "images": output,
        "limitations": "Detects broad plant-part regions only. It is not trained to identify GrowPlot species or diseases and does not confirm plant health.",
    }
