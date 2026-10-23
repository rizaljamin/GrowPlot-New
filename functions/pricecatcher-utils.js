export function parseCsvLine(line) {
  const cells = [];
  let value = '';
  let quoted = false;
  for (let i = 0; i < line.length; i += 1) {
    const char = line[i];
    if (char === '"') {
      if (quoted && line[i + 1] === '"') { value += '"'; i += 1; }
      else quoted = !quoted;
    } else if (char === ',' && !quoted) {
      cells.push(value);
      value = '';
    } else value += char;
  }
  cells.push(value);
  return cells;
}

export function pricePerKg(price, unit) {
  const amount = Number(price);
  if (!Number.isFinite(amount) || amount < 0) return null;
  const normalized = String(unit || '').toLowerCase().replace(/[._]/g, ' ').replace(/\\s+/g, ' ').trim();
  if (/^(?:1\\s*)?(?:kg|kilogram|kilograms)$/.test(normalized)) return amount;
  const grams = normalized.match(/^(?:1\\s*)?(\\d+(?:\\.\\d+)?)\\s*(?:g|gram|grams)$/);
  if (grams) {
    const gramsPerItem = Number(grams[1]);
    return gramsPerItem > 0 ? amount * 1000 / gramsPerItem : null;
  }
  return null;
}

export function itemMatchesAliases(item, aliases) {
  const name = String(item || '').normalize('NFD').replace(/[\\u0300-\\u036f]/g, '').toLowerCase();
  return (Array.isArray(aliases) ? aliases : []).some(alias => {
    const term = String(alias || '').normalize('NFD').replace(/[\\u0300-\\u036f]/g, '').toLowerCase().trim();
    if (term.length < 3) return false;
    const escaped = term.replace(/[.*+?^${}()|[\\]\\\\]/g, '\\$&');
    return new RegExp('(?:^|[^a-z0-9])' + escaped + '(?:$|[^a-z0-9])', 'i').test(name);
  });
}

export function monthlyPriceSummary(month, sourceUrl, stats, aliases) {
  const matched = (Array.isArray(stats) ? stats : []).filter(row =>
    row && itemMatchesAliases(row.item, aliases) && Number(row.count) > 0 && Number.isFinite(Number(row.sum))
  ).map(row => ({
    name: String(row.item),
    averageRmPerKg: Number((Number(row.sum) / Number(row.count)).toFixed(2)),
    observations: Number(row.count)
  }));
  const observations = matched.reduce((sum, row) => sum + row.observations, 0);
  const total = matched.reduce((sum, row) => sum + row.averageRmPerKg * row.observations, 0);
  return {
    found: matched.length > 0,
    month,
    source: 'Malaysia PriceCatcher · retail price observations',
    sourceUrl,
    unit: 'RM/kg',
    averageRmPerKg: observations ? Number((total / observations).toFixed(2)) : null,
    observations,
    items: matched
  };
}
