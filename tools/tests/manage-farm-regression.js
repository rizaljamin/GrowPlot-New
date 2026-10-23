const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const script = html.slice(html.indexOf('<script>') + 8, html.indexOf('</script>'));
const line = (name) => {
  const start = script.indexOf(`function ${name}(`);
  assert.notEqual(start, -1, `missing ${name}`);
  const next = script.indexOf('function ', start + 10);
  return script.slice(start, next === -1 ? script.length : next).trim();
};
const context = {
  farmPlotFilter: 'plot-1', farmGreenhouseFilter: 'all', farmSectionFilter: 'all',
  plots: [{ id: 'plot-1', name: 'Plot 1' }, { id: 'plot-2', name: 'Plot 2' }],
  farmEscape: (value) => String(value), plotLabel: (plot) => plot.name,
  document: { getElementById: () => ({ options: [
    { value: '1', textContent: 'Greenhouse 1' },
    { value: '2', textContent: 'Greenhouse 2' },
  ] }) },
  farmDateLabel: (value) => value,
};
vm.createContext(context);
vm.runInContext([line('farmScopeMatches'), line('farmScopeFilterMarkup'), line('farmLocalIsoDate'), line('farmMaintenanceCue')].join('\n'), context);

const matches = (record) => vm.runInContext(`farmScopeMatches(${JSON.stringify(record)})`, context);
assert.equal(matches({ plotId: 'plot-1', greenhouse: '2', section: 5 }), true, 'all-scope filter includes other greenhouses and sections');
context.farmGreenhouseFilter = '2';
context.farmSectionFilter = '3';
assert.equal(matches({ plotId: 'plot-1', greenhouse: '2', section: 3 }), true, 'exact section is visible');
assert.equal(matches({ plotId: 'plot-1', greenhouse: '2', section: 'all' }), true, 'all-section records remain visible in section view');
assert.equal(matches({ plotId: 'plot-1', greenhouse: '1', section: 3 }), false, 'other greenhouse is hidden');
assert.equal(matches({ plotId: 'plot-2', greenhouse: '2', section: 3 }), false, 'other plot is hidden');

const filterHtml = vm.runInContext('farmScopeFilterMarkup()', context);
assert.match(filterHtml, /class="farm-scope-filters"/);
assert.match(filterHtml, /data-farm-filter="plot"/);
assert.match(filterHtml, /data-farm-filter="greenhouse"/);
assert.match(filterHtml, /data-farm-filter="section"/);
assert.doesNotMatch(filterHtml, /\\"/, 'scope controls render valid HTML attributes');

const cue = (record) => vm.runInContext(`farmMaintenanceCue(${JSON.stringify(record)})`, context);
assert.match(cue({ status: 'Completed', completedAt: '2026-10-14' }), /Completed · 2026-10-14/);
assert.match(cue({ status: 'Planned', scheduledDate: '2000-01-01' }), /Overdue/);
assert.match(cue({ status: 'Planned', scheduledDate: context.farmLocalIsoDate() }), /Due today/);
assert.match(cue({ status: 'Planned', scheduledDate: '2999-01-01' }), /Upcoming/);

context.farmMaintenanceTasks = [{ id: 'task-1', status: 'Planned' }];
context.farmManagementDeletedIds = [];
context.renderManageFarm = () => { context.rendered = true; };
context.syncFarmPlantingsToDrive = () => { context.synced = true; };
context.showSaveToast = (message) => { context.toast = message; };
vm.runInContext(line('completeFarmMaintenance'), context);
vm.runInContext("completeFarmMaintenance('task-1')", context);
assert.equal(context.farmMaintenanceTasks[0].status, 'Completed');
assert.match(context.farmMaintenanceTasks[0].completedAt, /^\d{4}-\d{2}-\d{2}$/);
assert.equal(context.rendered && context.synced, true, 'completion rerenders and syncs records');
assert.equal(context.toast, 'Maintenance task completed');

const renderStart = script.indexOf('function renderManageFarm(editRecord=null)');
const renderEnd = script.indexOf('function persistFarmPlantingsLocally()', renderStart);
const renderFarm = script.slice(renderStart, renderEnd);
assert.ok(renderStart >= 0 && renderEnd > renderStart, 'renderManageFarm exists');
assert.ok(renderFarm.indexOf('const existing=editRecord||{};') < renderFarm.indexOf('const plotsHtml='), 'crop screen initializes edit state before generating filters');

const farmStart = script.indexOf('function manageFarmTabsMarkup(){');
const farmTabEnd = script.indexOf('function renderManageFarm(editRecord=null)', farmStart);
const farmHelpersStart = script.lastIndexOf('let plotManagementSettingsAnchor=null,plotManagementSettingsOpen=false;', farmStart);
const farmTabs = script.slice(farmHelpersStart, farmTabEnd);
assert.ok(farmTabs.indexOf('>Plot</button>') < farmTabs.indexOf('>Crop Planning</button>'), 'Plot tab appears before Crop Planning');
assert.match(farmTabs, /function movePlotManagementSectionToManageFarm\(\)[\s\S]*?host\.appendChild\(section\)/, 'Manage Farm reuses the Settings Plot Management section');
assert.match(farmTabs, /function restorePlotManagementSection\(\)/, 'Plot Management returns to Settings when leaving its Manage Farm tab');
assert.match(renderFarm, /id="manageFarmPlotHost"/, 'Plot tab provides a host for the shared Settings section');
assert.match(renderFarm, /farmActiveMenu==='plot'\)\{restorePlotManagementSection\(\);root\.innerHTML=/, 'reopening or rerendering Plot restores the shared section before rebuilding its host');
const showPanel = line('showPanel');
assert.match(showPanel, /else restorePlotManagementSection\(\)/, 'opening another main menu restores Plot Management to Settings');
assert.match(showPanel, /if\(kind===\'maintenance\'\)farmActiveMenu=\'plot\'/, 'opening Manage Farm resets its default tab to Plot');
const sharedPlotSection = { parentNode: null, open: true, classes: new Set(), classList: {
  add(name) { sharedPlotSection.classes.add(name); }, remove(name) { sharedPlotSection.classes.delete(name); },
} };
const detach = (node) => {
  if (!node.parentNode) return;
  const at = node.parentNode.children.indexOf(node);
  if (at >= 0) node.parentNode.children.splice(at, 1);
  node.parentNode = null;
};
const settingsParent = { children: [sharedPlotSection], insertBefore(node, before) {
  detach(node);
  const at = before ? this.children.indexOf(before) : -1;
  this.children.splice(at < 0 ? this.children.length : at, 0, node);
  node.parentNode = this;
} };
sharedPlotSection.parentNode = settingsParent;
const plotHost = { children: [], appendChild(node) { detach(node); this.children.push(node); node.parentNode = this; } };
const moveContext = {
  plotManagementSettingsAnchor: null, plotManagementSettingsOpen: false,
  document: {
    getElementById: (id) => id === 'plotManagementSection' ? sharedPlotSection : id === 'manageFarmPlotHost' ? plotHost : null,
    createComment: () => {
      const anchor = { parentNode: null };
      Object.defineProperty(anchor, 'nextSibling', { get() {
        const at = this.parentNode ? this.parentNode.children.indexOf(this) : -1;
        return at >= 0 ? this.parentNode.children[at + 1] || null : null;
      } });
      return anchor;
    },
  },
};
vm.createContext(moveContext);
vm.runInContext([line('movePlotManagementSectionToManageFarm'), line('restorePlotManagementSection')].join('\n'), moveContext);
vm.runInContext('movePlotManagementSectionToManageFarm()', moveContext);
assert.equal(plotHost.children[0], sharedPlotSection, 'Manage Farm moves the existing Settings Plot Management section');
assert.equal(sharedPlotSection.open, true, 'Plot Management opens while shown as a Manage Farm tab');
sharedPlotSection.open = false;
vm.runInContext('restorePlotManagementSection()', moveContext);
assert.equal(settingsParent.children[1], sharedPlotSection, 'leaving the Plot tab restores the same Settings section in place');
assert.equal(sharedPlotSection.open, true, 'restoring Plot Management preserves its Settings accordion state');
assert.equal(sharedPlotSection.classes.has('manage-farm-plot-panel'), false, 'restored section clears Manage Farm styling');
const farmContext = {
  document: { getElementById: (id) => farmElements[id] || { options: [] } },
  restorePlotManagementSection: () => {},
  farmActiveMenu: 'crops', farmEditingId: '', farmEditingManagementId: '', farmEditingManagementType: '',
  farmPlotFilter: 'plot-1', farmGreenhouseFilter: '1', farmSectionFilter: 'all',
  farmPlantings: [], farmPlantingDeletedIds: [], farmIrrigationSchedules: [], farmMaintenanceTasks: [], farmManagementDeletedIds: [],
  farmCropCatalog: [{ id: 'chilli', name: 'Chilli', scientific: 'Capsicum annuum', varieties: 'Kulai', img: '', days: [56, 70], basis: 'after planting' }],
  plots: [{ id: 'plot-1', name: 'Plot 1' }],
  farmEscape: (value) => String(value ?? ''), plotLabel: (plot) => plot.name,
  farmScopeMatches: () => true, farmScopeFilterMarkup: () => '',
  farmDateLabel: (value) => String(value || '—'), farmExpectedDate: () => null, farmHarvestStart: () => null,
  saveFarmPlanting: () => {}, farmAction: () => {},
};
const farmRoot = { hidden: true, innerHTML: '', querySelectorAll: () => [] };
const farmForm = { addEventListener: () => {}, elements: { plotId: { addEventListener: () => {} }, greenhouse: { addEventListener: () => {}, value: '1' } } };
const farmPicker = { hidden: true, addEventListener: () => {} };
const farmAddForm = { addEventListener: () => {}, elements: { name: { value: '', addEventListener: () => {} } } };
const farmAddPopup = { hidden: true, addEventListener: () => {} };
const farmElements = {
  manageFarmContent: farmRoot,
  gh: { options: [{ value: '1', textContent: 'Greenhouse 1' }], value: '1' },
  farmPlantingForm: farmForm, farmCropPopup: farmPicker, farmAddCropForm: farmAddForm, farmAddCropPopup: farmAddPopup, farmCropWebSearch: { href: '', addEventListener: () => {} },
  farmOpenCropPicker: { addEventListener: () => {} }, farmCloseCropPicker: { addEventListener: () => {} }, farmOpenAddCrop: { addEventListener: () => {} }, farmCloseAddCrop: { addEventListener: () => {} }, farmCancelAddCrop: { addEventListener: () => {} },
};
vm.createContext(farmContext);
vm.runInContext(script.slice(farmStart, renderEnd), farmContext);
vm.runInContext('renderManageFarm()', farmContext);
assert.equal(farmRoot.hidden, false, 'opening Manage Farm reveals its content');
assert.match(farmRoot.innerHTML, /Crop Planning/);
assert.match(farmRoot.innerHTML, /Irrigation &amp; Fertiliser/);
assert.match(farmRoot.innerHTML, /Maintenance/);

assert.match(script, /a\.scheduledDate\|\|a\.startDate/ , 'irrigation schedules sort by first scheduled date');
const manifest = fs.readFileSync('app/src/main/AndroidManifest.xml', 'utf8');
const java = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
assert.match(manifest, /android\.permission\.ACCESS_FINE_LOCATION/, 'GrowPlot declares foreground GPS access');
assert.match(manifest, /android\.permission\.ACCESS_COARSE_LOCATION/, 'GrowPlot declares approximate foreground location access');
assert.doesNotMatch(manifest, /android\.permission\.ACCESS_BACKGROUND_LOCATION/, 'map use does not request unnecessary background location');
assert.match(java, /googleMaps\.setPackage\("com\.google\.android\.apps\.maps"\)/, 'plot map links prefer the Google Maps app');
assert.match(java, /catch \(Exception mapsUnavailable\)[\s\S]*?new Intent\(Intent\.ACTION_VIEW, maps\)/, 'plot map links fall back to the web map when the app is unavailable');
const cropSave = line('saveFarmCrop');
assert.match(cropSave, /farmCustomCrops\.push\(crop\)/, 'new crop enters the selectable catalogue');
assert.match(cropSave, /max>3650/, 'harvest estimates have an upper bound');
assert.match(cropSave, /That crop is already in the list/, 'duplicate crop names are rejected');
assert.match(renderFarm, /id="farmOpenAddCrop"/, 'Add crop button sits beside the crop picker');
assert.match(renderFarm, /id="farmAddCropForm"/, 'crop details have a dedicated entry form');
assert.match(line('persistFarmPlantingsLocally'), /growplotCustomCrops_v1/, 'custom crop catalogue is persisted locally');
assert.match(line('syncFarmPlantingsToDrive'), /customCrops:farmCustomCrops/, 'custom crop catalogue is sent to Drive');
const farmJava = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
assert.match(farmJava, /response\.put\("customCrops"/, 'Drive load returns the custom catalogue');
assert.match(farmJava, /management\.put\("customCrops", customCrops\)/, 'Drive save stores the custom catalogue');
assert.match(farmJava, /openCropInfoSearch/, 'crop web search opens outside the app WebView');

assert.match(html, /id="marketBuyerList"/, 'Marketplace exposes Buyer List');
assert.match(html, /id="marketHistoricalPrice"/, 'Marketplace exposes Historical Price');
assert.match(html, /farmCropCatalog\.map\(c=>/, 'Historical Price crop options use the shared crop catalogue');
assert.match(html, /ami\.fama\.gov\.my\/web-awam/, 'Historical Price opens the official FAMA AMI portal');
assert.match(html, /Price unit: RM\/kg/, 'Historical Price states the requested price unit');
assert.match(farmJava, /openFamaMarketPortal/, 'FAMA AMI portal opens through the Android bridge');

console.log('PASS: farm scope filters, Plot default, shared plot management, due cues, completion dates, irrigation ordering, and Google Maps routing/location permissions.');
