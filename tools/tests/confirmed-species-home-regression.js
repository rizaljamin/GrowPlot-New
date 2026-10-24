const fs = require('node:fs');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const build = fs.readFileSync('tools/build-apk.sh', 'utf8');

assert.match(html, /const defaultPlantIds=plantIds\.map\(item=>\(\{\.\.\.item\}\)\)/,
  'each plot render starts from the original camera identity defaults');
assert.match(html, /function loadConfirmedPlantIds\(plotId=selectedPlotId\)[\s\S]*?diagnosisSpeciesKey\(index\+1,plotId\)[\s\S]*?identity\?\.confirmedAt[\s\S]*?identity\?\.source!=='plantnet_image'[\s\S]*?plant\.malay=identity\.name[\s\S]*?plant\.scientific=identity\.scientific/,
  'only user-confirmed species saved for the active plot are restored to camera labels');
assert.match(html, /function render\(\)\{[\s\S]*?loadConfirmedPlantIds\(selectedPlotId\);[\s\S]*?cameraRoot\.innerHTML=''/,
  'home camera cards hydrate confirmed species before rendering');
assert.match(html, /function updateCameraSpeciesDisplay\(camera\)[\s\S]*?querySelector\('\.plant-id b'\)[\s\S]*?querySelector\('\.plant-id em'\)/,
  'the currently rendered home card updates immediately after confirmation');
assert.match(html, /function loadConfirmedPlantIds\(plotId=selectedPlotId\)[\s\S]*?plant\.identityConfirmed=true;return;let suggestion=null[\s\S]*?diagnosisSpeciesCandidates\(report\)\[0\][\s\S]*?plant\.identitySuggested=true/,
  'when identity is not confirmed, the home card can show the diagnosis candidate clearly marked unconfirmed');
assert.match(html, /plant\.identitySuggested\?'DIAGNOSIS SUGGESTION · NOT CONFIRMED'/,
  'suggested species on the home card is explicitly labeled as not confirmed');
assert.match(html, /function confirmDiagnosisSpeciesChoice\(camera,choice\)[\s\S]*?localStorage\.setItem\(diagnosisSpeciesKey\(camera\),JSON\.stringify\(confirmed\)\)[\s\S]*?loadConfirmedPlantIds\(selectedPlotId\);updateCameraSpeciesDisplay\(camera\)/,
  'diagnosis confirmation persists and refreshes the matching home species label');
assert.match(build, /node "\$ROOT\/tools\/tests\/confirmed-species-home-regression\.js"/,
  'the confirmed-species home-label regression runs in the build gate');

console.log('PASS: confirmed species persist per plot and appear on home CCTV cards.');
