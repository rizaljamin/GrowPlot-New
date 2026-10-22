'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const index = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const cv = fs.readFileSync('app/src/main/assets/leaf-cv.js', 'utf8');
const proxy = fs.readFileSync('backend/server.js', 'utf8');
const model = fs.readFileSync('backend/yolo/yolo_server.py', 'utf8');
const docker = fs.readFileSync('backend/yolo/Dockerfile', 'utf8');
const build = fs.readFileSync('tools/build-apk.sh', 'utf8');

assert.match(index, /await engine\.runYolo\(camera\)/,
  'Run Diagnosis must trigger YOLO analysis automatically');
assert.match(index, /setGrowPlotDiagnosisProgress\(camera,'yolo'/,
  'Run Diagnosis must track the YOLO request status');
assert.doesNotMatch(index, /Analysis stages|growPlotDiagnosisStages|diagnosis-hourglass/,
  'Run Diagnosis must not show the generic six-stage analysis card');
assert.match(cv, /getDiagnosisApiConfig\(\)/,
  'YOLO analysis must use the configured diagnosis proxy');
assert.match(cv, /Authorization:'Bearer '\+token/,
  'YOLO requests must use the configured app-to-proxy token');
assert.match(cv, /YOLO-World plant-part detection/,
  'The diagnosis screen must identify the YOLO result clearly');
assert.match(proxy, /app\.post\('\/v1\/yolo', authorize/,
  'The diagnosis proxy must authenticate YOLO requests');
assert.match(proxy, /GROWPLOT_YOLO_SERVICE_URL/,
  'The proxy must call the configured YOLO model service');
assert.match(model, /YOLOWorld\(MODEL_NAME\)/,
  'The model service must load a real YOLO-World model');
assert.match(model, /model\.set_classes\(CLASSES\)/,
  'The model must use the supported plant-part vocabulary');
assert.match(model, /"plant", "leaf", "flower", "fruit"/,
  'The model vocabulary must remain broad and must not claim disease classes');
assert.match(docker, /yolov8s-world\.pt/,
  'The inference service must declare its pretrained YOLO-World weights');
assert.match(build, /GROWPLOT_DIAGNOSIS_API_URL/,
  'Builds must pass the diagnosis endpoint through without hardcoding it');
console.log('Run Diagnosis YOLO-World wiring and proxy regression checks passed.');
