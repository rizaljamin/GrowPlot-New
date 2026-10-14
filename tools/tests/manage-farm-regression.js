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

assert.match(script, /a\.scheduledDate\|\|a\.startDate/ , 'irrigation schedules sort by first scheduled date');
console.log('PASS: farm scope filters, due cues, quick completion with date, and irrigation ordering.');
