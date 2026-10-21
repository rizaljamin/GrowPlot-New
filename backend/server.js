import express from 'express';
import multer from 'multer';

const app = express();
const port = Number(process.env.PORT || 8080);
const MAX_PHOTOS = 5;
const MAX_FILE_BYTES = 10 * 1024 * 1024;
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { files: MAX_PHOTOS, fileSize: MAX_FILE_BYTES, fields: 10 },
  fileFilter: (_req, file, cb) => cb(null, ['image/jpeg', 'image/png'].includes(file.mimetype))
});

app.disable('x-powered-by');
app.use((req, res, next) => {
  res.set('Access-Control-Allow-Origin', 'https://appassets.androidplatform.net');
  res.set('Access-Control-Allow-Headers', 'Authorization, Content-Type');
  res.set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  next();
});
app.get('/healthz', (_req, res) => res.json({ ok: true }));

function authorize(req, res, next) {
  const expected = process.env.GROWPLOT_PROXY_TOKEN;
  if (!expected) return res.status(503).json({ error: 'Proxy access is not configured.' });
  if (req.get('authorization') !== `Bearer ${expected}`) return res.status(401).json({ error: 'Unauthorized.' });
  next();
}

function safeJson(value, fallback = {}) {
  try { return JSON.parse(value || '{}'); } catch { return fallback; }
}

async function kindwiseCheck(images) {
  const apiKey = process.env.KINDWISE_CROP_HEALTH_API_KEY;
  if (!apiKey) return { provider: 'Kindwise crop.health', status: 'not_configured' };
  const response = await fetch('https://crop.kindwise.com/api/v1/identification', {
    method: 'POST',
    headers: { 'Api-Key': apiKey, 'Content-Type': 'application/json' },
    body: JSON.stringify({ images: images.map(file => file.buffer.toString('base64')) }),
    signal: AbortSignal.timeout(45000)
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(`Kindwise returned HTTP ${response.status}`);
  const result = payload.result || {};
  const candidates = [
    ...(result.crop?.suggestions || []).map(x => ({ type: 'crop', name: x.name, confidence: x.probability })),
    ...(result.disease?.suggestions || []).map(x => ({ type: 'health issue', name: x.name, confidence: x.probability, details: x.details }))
  ];
  return { provider: 'Kindwise crop.health', status: 'ok', model: payload.model_version || null, candidates };
}

async function plantNetCheck(images) {
  const apiKey = process.env.PLANTNET_API_KEY;
  if (!apiKey) return { provider: 'Pl@ntNet', status: 'not_configured' };
  const form = new FormData();
  for (let i = 0; i < images.length; i++) {
    const file = images[i];
    form.append('images', new Blob([file.buffer], { type: file.mimetype }), file.originalname || `plant-${i + 1}.jpg`);
  }
  form.append('lang', 'en');
  form.append('nb-results', '5');
  const response = await fetch(`https://my-api.plantnet.org/v2/diseases/identify?api-key=${encodeURIComponent(apiKey)}`, {
    method: 'POST', body: form, signal: AbortSignal.timeout(45000)
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(`Pl@ntNet returned HTTP ${response.status}`);
  return {
    provider: 'Pl@ntNet', status: 'ok', model: payload.version || null,
    candidates: (payload.results || []).map(x => ({ type: 'disease / pest', name: x.description || x.name, code: x.name, confidence: x.score }))
  };
}

async function yoloCheck(images) {
  const endpoint = (process.env.GROWPLOT_YOLO_SERVICE_URL || '').replace(/\/$/, '');
  if (!endpoint) {
    return { provider: 'YOLO-World', status: 'not_configured', message: 'The YOLO inference service is not configured on the diagnosis server.' };
  }
  const form = new FormData();
  for (let i = 0; i < images.length; i++) {
    const file = images[i];
    form.append('photos', new Blob([file.buffer], { type: file.mimetype }), file.originalname || `plant-${i + 1}.jpg`);
  }
  const headers = {};
  if (process.env.GROWPLOT_YOLO_SERVICE_TOKEN) headers.Authorization = `Bearer ${process.env.GROWPLOT_YOLO_SERVICE_TOKEN}`;
  const response = await fetch(`${endpoint}/v1/yolo`, {
    method: 'POST', headers, body: form, signal: AbortSignal.timeout(120000)
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(`YOLO service returned HTTP ${response.status}`);
  return payload;
}

app.post('/v1/yolo', authorize, upload.array('photos', MAX_PHOTOS), async (req, res) => {
  const images = req.files || [];
  if (!images.length) return res.status(400).json({ error: 'Attach at least one photo.' });
  try {
    res.json(await yoloCheck(images));
  } catch (error) {
    res.status(502).json({ provider: 'YOLO-World', status: 'error', message: error.message || 'YOLO inference failed.' });
  }
});

app.post('/v1/diagnose', authorize, upload.array('photos', MAX_PHOTOS), async (req, res) => {
  const images = req.files || [];
  if (!images.length) return res.status(400).json({ error: 'Attach at least one photo.' });
  if (images.some(file => !['image/jpeg', 'image/png'].includes(file.mimetype))) {
    return res.status(415).json({ error: 'Photos must be JPEG or PNG.' });
  }
  if (images.reduce((n, file) => n + file.size, 0) > 50 * 1024 * 1024) {
    return res.status(413).json({ error: 'Total photo size exceeds 50 MB.' });
  }
  const context = safeJson(req.body.context);
  const checks = await Promise.allSettled([kindwiseCheck(images), plantNetCheck(images), yoloCheck(images)]);
  const providers = checks.map((item, index) => {
    if (item.status === 'fulfilled') return item.value;
    return { provider: ['Kindwise crop.health', 'Pl@ntNet', 'YOLO-World'][index], status: 'error', message: item.reason?.message || 'Provider request failed.' };
  });
  res.json({
    generatedAt: new Date().toISOString(),
    context: { species: context.species || '', symptoms: context.symptoms || '', pixelAnalysis: context.pixelAnalysis || null },
    providers,
    note: 'Provider models were queried with photos. Context fields are included for the app report and are not represented as model inputs.'
  });
});

app.use((error, _req, res, _next) => {
  const status = error instanceof multer.MulterError ? (error.code === 'LIMIT_FILE_SIZE' ? 413 : 400) : 500;
  res.status(status).json({ error: status === 500 ? 'Diagnosis service error.' : error.message });
});

app.listen(port, '0.0.0.0', () => console.log(`GrowPlot diagnosis proxy listening on ${port}`));
