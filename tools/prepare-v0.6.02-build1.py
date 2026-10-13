#!/usr/bin/env python3
"""Apply reviewed v0.6.02 Build 1 crop-planning layout to Build 5 baseline."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
html = root / "app/src/main/assets/index.html"
version = root / "version.properties"
s = html.read_text(encoding="utf-8")
if '<h4>Active Crops</h4>' in s and '<h4>Recommended Crops</h4>' in s:
    print("Crop planning layout already present")
else:
    old = 'const existing=editRecord||{};'
    new = """const activeRecordsHtml=records.filter(r=>['Nursery','Transplanted','Growing','Harvesting'].includes(r.status)).map(r=>{const c=farmCropCatalog.find(x=>x.id===r.cropId)||farmCropCatalog[0];return `<article class="farm-record"><h5>${farmEscape(c.name)} · ${farmEscape(r.status)}</h5><p>${farmEscape(plotLabel(plots.find(p=>p.id===r.plotId)||plots[0]))} · Greenhouse ${farmEscape(r.greenhouse||'1')} · ${String(r.section||1)==='all'?'All sections':'Section '+String(r.section||1).padStart(2,'0')}</p><button class="farm-secondary" type="button" data-farm-action="edit" data-id="${farmEscape(r.id)}">Edit crop</button></article>`}).join('');const existing=editRecord||{};"""
    old_root = 'root.innerHTML=manageFarmTabsMarkup()+`<p class="farm-intro">Choose a crop by photo first, then assign it to a plot, greenhouse, and one or all sections.</p>${cropCatalogueSection}<p id="farmDriveStatus"'
    new_root = 'root.innerHTML=manageFarmTabsMarkup()+`<h4>Active Crops</h4>${activeRecordsHtml||\'<div class="farm-empty">No active crops for this plot yet.</div>\'}<p class="farm-intro">Select a crop and plan its planting details below.</p><p id="farmDriveStatus"'
    old_end = '</form><p id="farmSaveStatus" class="plot-status" role="status"></p></div><h4>Assigned crops · ${farmEscape(plotLabel(plots.find(p=>p.id===farmPlotFilter)||plots[0]))}</h4>${recordsHtml}`;'
    new_end = '</form><p id="farmSaveStatus" class="plot-status" role="status"></p></div><h4>Recommended Crops</h4>${cropCatalogueSection}<h4>All Planned &amp; Historical Crops · ${farmEscape(plotLabel(plots.find(p=>p.id===farmPlotFilter)||plots[0]))}</h4>${recordsHtml}`;'
    for before, after in ((old,new),(old_root,new_root),(old_end,new_end)):
        n=s.count(before)
        if n!=1: raise SystemExit(f"Expected exactly one matching source location, found {n}: {before[:75]}")
        s=s.replace(before,after,1)
    html.write_text(s,encoding="utf-8")
v=version.read_text(encoding="utf-8")
if "versionName=0.6.01 Build 5" in v and "versionCode=132" in v:
    v=v.replace("versionName=0.6.01 Build 5","versionName=0.6.02 Build 1").replace("versionCode=132","versionCode=133")
    version.write_text(v,encoding="utf-8")
elif "versionName=0.6.02 Build 1" not in v or "versionCode=133" not in v:
    raise SystemExit("Unexpected version baseline")
assert "<h4>Active Crops</h4>" in s and "<h4>Recommended Crops</h4>" in s
print("Prepared GrowPlot v0.6.02 Build 1")
