'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');

const index = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const cv = fs.readFileSync('app/src/main/assets/leaf-cv.js', 'utf8');
const bridge = fs.readFileSync('app/src/main/java/com/growplot/farm/VertexDiagnosisBridge.java', 'utf8');
const main = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
const gradle = fs.readFileSync('app/build.gradle', 'utf8');

const handlerAt = index.indexOf('async function runDiagnosisScreen(camera){');
assert(handlerAt >= 0, 'Run Diagnosis handler exists');
const handler = index.slice(handlerAt, index.indexOf('\nfunction renderDiagnosisModal', handlerAt));
const cvAt = handler.indexOf('cvReport=await engine.analyze(camera)');
const vertexAt = handler.indexOf('await runVertexDiagnosis(camera,cvReport)');
assert(cvAt >= 0 && vertexAt > cvAt,
  'Run Diagnosis must complete OpenCV before triggering Vertex with that report');
assert.match(index, /window\.GrowPlotVertex\.analyze\(requestId,imageData,cvReport\.vertexMimeType/,
  'The selected OpenCV photo must be submitted through the Android Firebase bridge');
assert.match(index, /OpenCV completed\\. Sending the same photo to Vertex AI/,
  'The UI must show that Vertex starts after OpenCV completes');
assert.match(index, /vertex-processing-icon/,
  'The Vertex result subsection must show an animated hourglass while the API is pending');
assert.match(index, /vertexHourglassSpin/,
  'The pending Vertex hourglass must have a visible animation');
assert.match(index, /aria-busy="true"/,
  'The pending Vertex subsection must expose its busy state accessibly');
assert.match(cv, /vertexCanvas\.toDataURL\('image\/jpeg',0\.84\)/,
  'The Vertex photo must be derived from the same loaded source photo as OpenCV');
assert.match(cv, /vertexImageBase64=vertexCanvas\.toDataURL/,
  'OpenCV must return the prepared source photo transiently for the Vertex request');
assert.match(main, /new VertexDiagnosisBridge\(this, web\)/,
  'MainActivity must expose the authenticated Vertex bridge');
assert.match(gradle, /implementation 'com\.google\.firebase:firebase-functions'/,
  'The Android app must include Firebase callable support');
assert.match(bridge, /FirebaseAuth\.getInstance\(\)\.getCurrentUser\(\)/,
  'The bridge must require an authenticated GrowPlot user');
assert.match(bridge, /getHttpsCallable\("vertexImageDiagnosis"\)/,
  'The bridge must call the deployed Firebase function');
assert.match(bridge, /FirebaseFunctions\.getInstance\(REGION\)/,
  'The callable must use the us-central1 Firebase Functions region');
const vertexContextStart = index.indexOf('function vertexDiagnosisContext(camera){');
const vertexContextEnd = index.indexOf('\nfunction updateVertexDiagnosisResult', vertexContextStart);
const vertexContext = index.slice(vertexContextStart, vertexContextEnd);
assert.match(vertexContext, /return \{symptoms\}/,
  'Vertex should receive symptom context without a pre-confirmed species name');
assert.doesNotMatch(vertexContext, /species|scientific|chosen/,
  'A saved plant identity must not be sent as Vertex context');
assert.doesNotMatch(cv, /plantSpeciesMarkup\(report/,
  'The Pl@ntNet/Kindwise provider area must not duplicate the saved identity card');
assert.match(index, /color_breakdown_percent/,
  'The Vertex result UI must display the response fields returned by the callable');
console.log('OpenCV then Vertex callable wiring regression checks passed.');
