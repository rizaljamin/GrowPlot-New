"""GrowPlot vision proxy. Run with OPENAI_API_KEY and GROWPLOT_ALLOWED_ORIGIN configured."""
import base64
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.request import Request, urlopen

KEY = os.environ.get("OPENAI_API_KEY", "")
MODEL = os.environ.get("OPENAI_VISION_MODEL", "gpt-4.1-mini")
ALLOWED = os.environ.get("GROWPLOT_ALLOWED_ORIGIN", "https://appassets.androidplatform.net")
PORT = int(os.environ.get("PORT", "8080"))
MAX_BYTES = 5_000_000
PROMPT = """Analyse this real plant photograph. Identify plant and pot/polybag separately,
then visible individual leaf candidates. Return JSON with keys:
plant_name (string, say uncertain if needed), plant_confidence (low/medium/high),
plant_box ([x1,y1,x2,y2] or null), container_box (same or null),
leaves (array of up to 20 objects with id integer, box [x1,y1,x2,y2],
condition short string, confidence low/medium/high),
observations (array of short strings), recommendations (array of short strings),
limitations (string).
All box coordinates must be integers normalized to 0..1000 relative to full image.
Boxes are approximate, not pixel segmentation. Do not invent unseen leaves,
diagnose a disease with certainty, or report percentages based on unsegmented background.
Return only valid JSON."""

class Handler(BaseHTTPRequestHandler):
    def send_json(self, code, obj):
        data = json.dumps(obj).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", ALLOWED)
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "POST, OPTIONS")
        self.end_headers()
        self.wfile.write(data)

    def do_OPTIONS(self):
        self.send_json(200, {})

    def do_POST(self):
        if self.path != "/api/plant-analysis":
            return self.send_json(404, {"error": "Not found"})
        if self.headers.get("Origin") not in (ALLOWED, None):
            return self.send_json(403, {"error": "Origin not permitted"})
        try:
            size = int(self.headers.get("Content-Length", "0"))
            if size < 1 or size > MAX_BYTES:
                return self.send_json(413, {"error": "Image request too large"})
            req = json.loads(self.rfile.read(size))
            image = req.get("image", "")
            if not isinstance(image, str) or not image.startswith("data:image/jpeg;base64,"):
                return self.send_json(400, {"error": "Expected JPEG data URL"})
            base64.b64decode(image.split(",", 1)[1], validate=True)
            if not KEY:
                return self.send_json(503, {"error": "Server OPENAI_API_KEY not configured"})
            payload = {"model": MODEL, "temperature": 0, "response_format": {"type": "json_object"},
                "messages": [{"role": "user", "content": [
                    {"type": "text", "text": PROMPT},
                    {"type": "image_url", "image_url": {"url": image, "detail": "high"}}
                ]}]}
            request = Request("https://api.openai.com/v1/chat/completions",
                data=json.dumps(payload).encode(),
                headers={"Authorization": "Bearer " + KEY, "Content-Type": "application/json"})
            with urlopen(request, timeout=65) as response:
                output = json.load(response)
            analysis = json.loads(output["choices"][0]["message"]["content"])
            self.send_json(200, {"analysis": analysis, "model": MODEL})
        except Exception as error:
            # Avoid exposing upstream request details, credentials, or image data.
            print("Analysis request failed:", type(error).__name__)
            self.send_json(502, {"error": "Vision analysis unavailable; check backend logs"})

if __name__ == "__main__":
    if not KEY:
        print("WARNING: OPENAI_API_KEY not set")
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
