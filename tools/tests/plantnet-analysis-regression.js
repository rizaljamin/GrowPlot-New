const fs=require('node:fs');
const assert=require('node:assert/strict');

const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
const java=fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java','utf8');

assert.match(html,/async function runDiagnosisScreen\(camera\)[\s\S]*?runPlantNetDiseaseCheck\(camera,plotId\)/,
  'Run diagnosis must start the external Pl@ntNet provider analysis');
assert.match(html,/function runPlantNetDiseaseCheck\(camera,plotId=selectedPlotId\)[\s\S]*?GrowPlotBridge\.diagnosePhotosWithPlantNet\(camera,plotId/,
  'the provider analysis must pass the active plot and camera to Android');
const providerMethod=java.slice(java.indexOf('@JavascriptInterface public void diagnosePhotosWithPlantNet('),java.indexOf('@JavascriptInterface public void identifyPlantSpeciesFromPhotos('));
assert.ok(providerMethod.length>0,'the Android provider analysis bridge must exist');
assert.match(providerMethod,/identifyPlantSpeciesWithPlantNet\(selectedImages\)/,
  'Pl@ntNet species candidates must use the saved user key');
assert.match(providerMethod,/response = identifyWithPlantNet\(selectedImages\)/,
  'Pl@ntNet disease analysis must use the saved user key');
assert.match(providerMethod,/requestKindwiseDiagnosis\(camera, id, selectedImages/,
  'Kindwise crop.health remains part of external provider analysis');
assert.doesNotMatch(providerMethod,/hasVertexAccessToken|requestVertexImageAnalysis|sendVertexResult/,
  'Pl@ntNet analysis must run independently of Vertex authentication without duplicating the Vertex request');
const speciesMethod=java.slice(java.indexOf('private JSONObject identifyPlantSpeciesWithPlantNet('),java.indexOf('private JSONObject identifyPlantSpeciesWithPlantId('));
const diseaseMethod=java.slice(java.indexOf('private JSONObject identifyWithPlantNet('),java.indexOf('private JSONObject identifyPlantSpeciesWithPlantNet('));
for(const [name,method] of [['species identification',speciesMethod],['disease analysis',diseaseMethod]]){
 assert.match(method,/pluginApiKey\("plantnet"\)/,name+' must read the encrypted Settings key');
 assert.match(method,/BuildConfig\.PLANTNET_API_KEY/,name+' must retain the configured build-key fallback');
}
assert.match(java,/savePluginApiKeys\(String payload\)[\s\S]*?plugin_api_keys_encrypted/,
  'the Settings key must remain encrypted at rest');

console.log('PASS: Pl@ntNet Settings key reaches species identification and disease analysis from Run diagnosis.');
