import assert from 'node:assert/strict';
import {parseCsvLine, pricePerKg, itemMatchesAliases, monthlyPriceSummary} from '../../functions/pricecatcher-utils.js';

assert.deepEqual(parseCsvLine('1,"Tomato, local","quoted ""word"""'), ['1', 'Tomato, local', 'quoted "word"']);
assert.equal(pricePerKg(4.5, '1 kg'), 4.5);
assert.equal(pricePerKg(2, '500 g'), 4);
assert.equal(pricePerKg(2, '250 grams'), 8);
assert.equal(pricePerKg(3, 'each'), null);
assert.equal(pricePerKg(-1, 'kg'), null);
assert.equal(itemMatchesAliases('Cili Merah', ['cili']), true);
assert.equal(itemMatchesAliases('Chili Powder', ['chili']), false);
assert.equal(itemMatchesAliases('Tomatoes', ['tomato']), false);
const summary = monthlyPriceSummary('2026-09', 'https://example.test/month.csv', [
  {item:'Tomato', sum:12, count:3},
  {item:'Tomato cherry', sum:100, count:2},
  {item:'Cabbage', sum:50, count:5}
], ['tomato']);
assert.equal(summary.found, true);
assert.equal(summary.averageRmPerKg, 22.4);
assert.equal(summary.observations, 5);
assert.equal(summary.items.length, 2);
assert.equal(summary.unit, 'RM/kg');
assert.equal(monthlyPriceSummary('2026-09', '', [], ['tomato']).found, false);
console.log('PriceCatcher utility regressions passed');
