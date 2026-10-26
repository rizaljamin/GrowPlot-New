'use strict';
const fs = require('node:fs');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const leafCv = fs.readFileSync('app/src/main/assets/leaf-cv.js', 'utf8');
const media = fs.readFileSync('app/src/main/assets/diagnosis-media.js', 'utf8');
const java = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');

assert.match(media, /function diagnosisMediaMatch\(a,b\)[\s\S]*?if\(ad&&bd\)return ad===bd/,
  'two different Drive documents must remain individually removable');
assert.match(media, /function mergeDiagnosisMediaLists\(current,incoming\)[\s\S]*?diagnosisMediaMatch\(old,item\)/,
  'the device copy and its Drive copy must merge into one diagnosis photo');
assert.match(media, /rememberRemovedDiagnosisMedia\(camera,plotId,docId,fileName,url\)/,
  'a deleted media item must remain hidden if Drive briefly returns a stale listing');
assert.match(html, /Remove photo/,
  'the photo action must be labelled clearly instead of implying every photo is a duplicate');
assert.match(leafCv, /photoTime\(item\)[\s\S]*?modifiedAt[\s\S]*?return photos\.reduce\(\(latest,item\)=>photoTime\(item\)>photoTime\(latest\)\?item:latest,photos\[0\]\)/,
  'OpenCV must choose the newest diagnosis photo by capture or modification time');
assert.match(leafCv, /report\.selectedPhoto=\{fileName:item\.fileName[\s\S]*?driveDocumentId:item\.driveDocumentId/,
  'the exact selected photo must be retained with its diagnosis record');

const provider = java.slice(
  java.indexOf('@JavascriptInterface public void diagnosePhotosWithPlantNet('),
  java.indexOf('@JavascriptInterface public void identifyPlantSpeciesFromPhotos(')
);
assert.match(provider, /loadDiagnosisPhotosFromDrive\(camera, id, 1\)/,
  'provider diagnosis must load only the latest Drive photo');
assert.match(provider, /loadDiagnosisPhotosFromDeviceIndex\(camera, id, 1\)/,
  'provider diagnosis must consider the newest local photo too');
assert.match(provider, /savedPhotos\.sort\(\(left, right\) -> Long\.compare\(right\.capturedAt, left\.capturedAt\)\)/,
  'the newest photo across Drive and device storage must be sent to providers');
assert.match(provider, /Collections\.singletonList\(candidates\.get\(0\)\)/,
  'Pl@ntNet and Kindwise must receive the same latest saved photo');

assert.match(media, /diagnosisPreviousComparison\(previous,report\)/,
  'a new diagnosis must compare against its previous saved diagnosis');
assert.match(html, /previousDiagnosisComparison:report\.previousDiagnosisComparison/,
  'the previous-diagnosis comparison must be saved with history');
assert.match(html, /report\.selectedPhoto\?\[report\.selectedPhoto\]:diagnosisPhotoReferences/,
  'history must retain the photo actually analysed');

console.log('Diagnosis-photo duplicate removal, latest-image selection, and comparison regressions passed.');
