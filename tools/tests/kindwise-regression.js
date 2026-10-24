const fs = require('fs');
const java = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

function requireMatch(pattern, source, message) {
  if (!pattern.test(source)) throw new Error(message);
}

requireMatch(/pluginApiKey\("kindwise"\)/, java, 'Kindwise must read the encrypted device key.');
requireMatch(/https:\/\/crop\.kindwise\.com\/api\/v1\/identification/, java, 'Kindwise crop.health endpoint is missing.');
requireMatch(/setRequestProperty\("Api-Key",\s*key\)/, java, 'Kindwise key must be sent in the documented Api-Key header.');
requireMatch(/Base64\.encodeToString\(images\.get\(i\),\s*Base64\.NO_WRAP\)/, java, 'Kindwise images must be submitted as base64.');
requireMatch(/Math\.min\(5,\s*images\.size\(\)\)/, java, 'Kindwise image requests must be limited to five images.');
requireMatch(/requestKindwiseDiagnosis\(camera,\s*id,\s*selectedImages/, java, 'Photo diagnosis must call Kindwise with the selected images.');
requireMatch(/requestKindwiseDiagnosis\(camera,\s*plotId,\s*selectedImages/, java, 'Video diagnosis must call Kindwise with the selected frames and photos.');
requireMatch(/window\.onKindwiseDiagnosis=function/, html, 'Kindwise result callback is missing.');
requireMatch(/diagnosisKindwiseProviderKey\(camera,plotId\)/, html, 'Kindwise result must be stored separately by plot and section.');
requireMatch(/diagnosisProviderResultsMarkup\(providerReport,kindwiseReport,camera,selectedPlotId\)/, html, 'Diagnosis screen must render both provider reports.');
requireMatch(/d\.treatment/, html, 'Kindwise treatment details must be available in the result card.');
const settingsStart = html.indexOf('<details id="pluginApiSettingsSection"');
const settingsEnd = html.indexOf('</details>', settingsStart);
const settings = html.slice(settingsStart, settingsEnd);
if (!settings.includes('<h4>Kindwise API keys</h4>')) {
  throw new Error('Kindwise services must appear under one settings heading.');
}
if (!settings.includes('id="plantidApiKey"') || !settings.includes('id="kindwiseApiKey"')) {
  throw new Error('Keep separate plant.id and crop.health API key fields.');
}
if ((settings.split("openPluginKeyPortal('kindwise')").length - 1) !== 1 ||
    settings.includes("openPluginKeyPortal('plantid')")) {
  throw new Error('Kindwise settings should show one shared account link, not duplicate provider links.');
}
if (settings.includes('Kindwise plant.id API key') || settings.includes('Kindwise · crop.health API key')) {
  throw new Error('Do not repeat the Kindwise brand on both service key labels.');
}

console.log('PASS: encrypted Kindwise key, selected photo/video requests, separate result storage, and treatment display.');
