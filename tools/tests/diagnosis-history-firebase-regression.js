'use strict';
const fs = require('node:fs');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const java = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
const rules = fs.readFileSync('firestore.rules', 'utf8');

assert.match(html, /GrowPlotBridge\.saveDiagnosisHistoryRecord\(camera,plotId,JSON\.stringify\(/,
  'each saved diagnosis record is sent to Android for Firestore persistence');
assert.match(html, /syncDiagnosisHistoryToFirebase\(camera,selectedPlotId\)/,
  'opening diagnosis loads and merges Firebase history');
assert.match(html, /window\.onDiagnosisHistoryLoaded=function[\s\S]*?localStorage\.setItem\(diagnosisHistoryKey\(camera,plotId\),JSON\.stringify\(merged\)\)/,
  'Firebase records are merged into the plot and camera local history');
assert.match(html, /function showDiagnosisHistoryOccurrence[\s\S]*?diagnosisAnalysisMarkup\(report\)[\s\S]*?diagnosisProviderResultsMarkup\(provider,details\.kindwiseResponse[\s\S]*?diagnosisVertexMarkup\(details\.vertexResponse/,
  'selecting a history record restores OpenCV, provider, and Vertex results');
assert.match(html, /async function runDiagnosisScreen\(camera\)[\s\S]*?historyId='diagnosis-'\+camera[\s\S]*?recordDiagnosisHistory\(camera,plotId,storedReport,'photo'\)/,
  'each OpenCV run creates a stable diagnosis history record');
assert.match(html, /delete storedReport\.vertexImageBase64;delete storedReport\.vertexMimeType/,
  'image payloads are not written into diagnosis history');
assert.match(java, /@JavascriptInterface public void saveDiagnosisHistoryRecord[\s\S]*?collection\("diagnosis_records"\)/,
  'Android saves diagnosis records to the dedicated Firestore collection');
assert.match(java, /@JavascriptInterface public void syncDiagnosisHistory[\s\S]*?loadFirebaseDiagnosisHistory/,
  'Android syncs local records and loads matching Firebase history');
assert.match(java, /whereEqualTo\("ownerUserId", uid\)\.whereEqualTo\("plotId", plotId\)\.whereEqualTo\("camera", camera\)/,
  'history loads are scoped to the signed-in user, plot, and camera');
assert.match(rules, /match \/diagnosis_records\/{recordDocumentId}[\s\S]*?resource\.data\.ownerUserId == request\.auth\.uid/,
  'Firestore rules restrict records to their authenticated owner');
console.log('Diagnosis Firebase history regression checks passed.');
