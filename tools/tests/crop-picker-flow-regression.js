const fs = require('fs');
const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
function requireMatch(pattern, source, message) {
  if (!pattern.test(source)) throw new Error(message);
}
requireMatch(/<button class="farm-crop-choose"[^>]*aria-haspopup="dialog"[\s\S]*?<svg viewBox="0 0 32 32"/, html, 'Plant-icon crop chooser button is missing.');
requireMatch(/id="farmCropPicker" class="farm-crop-picker" role="dialog" aria-modal="true"/, html, 'Crop list must appear in an accessible popup dialog.');
requireMatch(/<p class="crop-description">\$\{farmEscape\(c\.description\)\}<\/p>/, html, 'Crop choices must include descriptions.');
requireMatch(/<input type="hidden" name="cropId" value="\$\{picked\?farmEscape\(picked\.id\):''\}">/, html, 'Crop selection must be stored without a visible crop dropdown.');
requireMatch(/<div id="farmPlantingDetails"\$\{detailsHidden\}>/, html, 'Planting details must be hidden until a crop is selected.');
requireMatch(/function selectFarmCrop\(cropId\)[\s\S]*?form\.elements\.cropId\.value=crop\.id[\s\S]*?document\.getElementById\('farmPlantingDetails'\)\.hidden=false/, html, 'Selecting a crop must populate the selection and reveal planting details.');
requireMatch(/function openFarmCropPicker\(\)[\s\S]*?picker\.hidden=false/, html, 'Crop chooser must open the crop popup.');
requireMatch(/function closeFarmCropPicker\(\)[\s\S]*?picker\.hidden=true/, html, 'Crop popup must close accessibly.');
console.log('PASS: crop button, descriptive picker popup, hidden initial details, and selection-driven form population.');
