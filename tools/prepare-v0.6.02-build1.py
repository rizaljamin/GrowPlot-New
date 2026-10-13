#!/usr/bin/env python3
"""Apply reviewed v0.6.02 Build 1 crop-planning layout to Build 5 baseline."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
html = root / "app/src/main/assets/index.html"
version = root / "version.properties"
s = html.read_text(encoding="utf-8")
if '<h4>Active Crops</h4>' in s and 'id="farmCreateCropButton"' in s:
    print("Crop planning layout already present")
else:
    old = 'const existing=editRecord||{};'
    new = """const activeRecordsHtml=records.filter(r=>['Nursery','Transplanted','Growing','Harvesting'].includes(r.status)).map(r=>{const c=farmCropCatalog.find(x=>x.id===r.cropId)||farmCropCatalog[0];return `<article class="farm-record"><h5>${farmEscape(c.name)} · ${farmEscape(r.status)}</h5><p>${farmEscape(plotLabel(plots.find(p=>p.id===r.plotId)||plots[0]))} · Greenhouse ${farmEscape(r.greenhouse||'1')} · ${String(r.section||1)==='all'?'All sections':'Section '+String(r.section||1).padStart(2,'0')}</p><button class="farm-secondary" type="button" data-farm-action="edit" data-id="${farmEscape(r.id)}">Edit crop</button></article>`}).join('');const existing=editRecord||{};"""
    old_root = 'root.innerHTML=manageFarmTabsMarkup()+`<p class="farm-intro">Choose a crop by photo first, then assign it to a plot, greenhouse, and one or all sections.</p>${cropCatalogueSection}<p id="farmDriveStatus"'
    new_root = 'root.innerHTML=manageFarmTabsMarkup()+`<h4>Active Crops</h4>${activeRecordsHtml||\'<div class="farm-empty">No active crops for this plot yet.</div>\'}<p class="farm-intro">Select a crop and plan its planting details below.</p><p id="farmDriveStatus"'
    old_end = '</form><p id="farmSaveStatus" class="plot-status" role="status"></p></div><h4>Assigned crops · ${farmEscape(plotLabel(plots.find(p=>p.id===farmPlotFilter)||plots[0]))}</h4>${recordsHtml}`;'
    new_end = '</form><p id="farmSaveStatus" class="plot-status" role="status"></p></div><button type="button" id="farmCreateCropButton" class="farm-crop-toggle" aria-expanded="false">+ Create Crop</button><div id="farmCropCatalogue" hidden>${cropCatalogueSection}</div><h4>All Planned &amp; Historical Crops · ${farmEscape(plotLabel(plots.find(p=>p.id===farmPlotFilter)||plots[0]))}</h4>${recordsHtml}`;'
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
assert "<h4>Active Crops</h4>" in s and 'id="farmCreateCropButton"' in s
# Expose the crop catalogue on demand rather than keeping recommendations at page bottom.
needle = "document.getElementById('farmCropSearch').addEventListener('input',e=>{"
if "farmCreateCropButton').addEventListener" not in s:
    assert needle in s
    s=s.replace(needle, """document.getElementById('farmCreateCropButton').addEventListener('click',()=>{const list=document.getElementById('farmCropCatalogue'),btn=document.getElementById('farmCreateCropButton');list.hidden=!list.hidden;btn.textContent=list.hidden?'+ Create Crop':'Close Crop List';btn.setAttribute('aria-expanded',String(!list.hidden))});""" + needle,1)
if '.farm-crop-toggle{' not in s:
    s=s.replace('</style>', """.farm-crop-toggle{padding:10px 15px;border:0;border-radius:8px;background:#174a35;color:#fff;font-weight:800;margin:10px 0}
@keyframes gpBlink{50%{opacity:.2}}.gp-live-indicator{display:inline-flex;align-items:center;gap:5px;font:700 9px system-ui;color:#b12424}.gp-live-indicator:before{content:'';width:9px;height:9px;border-radius:50%;background:#dc3535;animation:gpBlink 1.2s infinite}.gp-live-indicator.available{color:#158040}.gp-live-indicator.available:before{background:#18ba56}
</style>""",1)
if 'data-gp-video=' not in s:
    needle='<span class="camname">SECTION ${id} · CCTV ${id}</span>'
    assert needle in s
    s=s.replace(needle,needle+'<span class="gp-live-indicator" data-gp-video="${i}" role="status">VIDEO OFFLINE</span>',1)
    s=s.replace('function render(){',"""window.onGrowPlotVideoStatus=function(camera,available){const el=document.querySelector('[data-gp-video="'+Number(camera)+'"]');if(el){el.classList.toggle('available',!!available);el.textContent=available?'VIDEO LIVE':'VIDEO OFFLINE'}};function render(){""",1)
html.write_text(s,encoding='utf-8')
print("Prepared GrowPlot v0.6.02 Build 1 with crop picker and CCTV video status UI")
