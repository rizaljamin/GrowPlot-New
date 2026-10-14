(function(){
'use strict';
const KEY='growplotPreviewUserPlots';
function owned(){try{const x=JSON.parse(localStorage.getItem(KEY)||'[]');return Array.isArray(x)?x:[]}catch(e){return []}}
function banner(){
 const existing=document.getElementById('growplotFirstPlotBanner');if(existing)existing.remove();
 const section=document.createElement('section');section.id='growplotFirstPlotBanner';
 section.setAttribute('aria-label','First plot setup');
 section.style.cssText='margin:14px auto;padding:18px;border-radius:16px;border:1px solid #badac2;background:#f0faf2;color:#153f2a;max-width:980px;box-sizing:border-box';
 const title=document.createElement('h2');title.textContent=owned().length?'Your GrowPlot workspace':'Welcome to GrowPlot';title.style.cssText='font-size:1.2rem;margin:0 0 8px';
 const desc=document.createElement('p');desc.textContent=owned().length?'Your own plots are listed alongside the demonstration plot.':'The Demo Plot is available to explore. Create your first plot to start managing your farm.';desc.style.cssText='margin:0 0 14px;line-height:1.5';
 const btn=document.createElement('button');btn.type='button';btn.textContent=owned().length?'Create Another Plot':'Create First Plot';btn.style.cssText='background:#166534;color:white;border:0;border-radius:10px;padding:12px 18px;font-weight:700;min-height:44px;cursor:pointer';
 btn.addEventListener('click',openCreate);
 section.append(title,desc,btn);
 const dash=document.getElementById('mainDashboard');if(dash&&dash.parentNode)dash.parentNode.insertBefore(section,dash);
}
function openCreate(){
 let overlay=document.getElementById('growplotCreateFirstPlot');if(overlay){overlay.hidden=false;return}
 overlay=document.createElement('div');overlay.id='growplotCreateFirstPlot';
 overlay.style.cssText='position:fixed;inset:0;z-index:99999;background:#0009;display:flex;align-items:center;justify-content:center;padding:20px';
 const form=document.createElement('form');form.style.cssText='background:white;border-radius:18px;padding:24px;max-width:420px;width:100%;box-shadow:0 16px 60px #0004';
 const heading=document.createElement('h2');heading.textContent='Create your first plot';heading.style.cssText='margin:0 0 12px;color:#153f2a';
 const help=document.createElement('p');help.textContent='Enter your plot name. Your Demo Plot remains available.';help.style.cssText='margin:0 0 16px';
 const input=document.createElement('input');input.required=true;input.maxLength=60;input.placeholder='e.g. Rubber Plot 1';input.setAttribute('aria-label','Plot name');input.style.cssText='width:100%;padding:12px;border:1px solid #aaa;border-radius:8px;box-sizing:border-box;font-size:16px';
 const status=document.createElement('p');status.setAttribute('role','status');
 const actions=document.createElement('div');actions.style.cssText='display:flex;gap:12px;justify-content:flex-end;margin-top:16px';
 const cancel=document.createElement('button');cancel.type='button';cancel.textContent='Cancel';cancel.onclick=()=>overlay.remove();
 const save=document.createElement('button');save.type='submit';save.textContent='Create Plot';save.style.cssText='background:#166534;color:white;border:0;border-radius:8px;padding:12px';
 actions.append(cancel,save);form.append(heading,help,input,status,actions);overlay.appendChild(form);document.body.appendChild(overlay);input.focus();
 form.addEventListener('submit',e=>{
 e.preventDefault();const name=input.value.trim();if(!name){status.textContent='Enter a plot name.';return}
 if(typeof plots==='undefined'||typeof savePlotData!=='function'){status.textContent='Plot manager is unavailable.';return}
 const mine=owned();const number=String(Math.max(0,...plots.map(p=>Number(p.number)||0))+1);
 const id='user-plot-'+Date.now().toString(36)+'-'+Math.floor(Math.random()*10000).toString(36);
 const plot={id,number,name,address:'',latitude:'',longitude:'',description:'',dataFeed:{enabled:false,folderUri:null}};
 plots.push(plot);selectedPlotId=id;
 if(!savePlotData()){plots.pop();status.textContent='Could not save plot on this device.';return}
 mine.push(id);localStorage.setItem(KEY,JSON.stringify(mine));
 if(typeof fillPlotSelectors==='function')fillPlotSelectors();
 if(typeof loadPlotForm==='function')loadPlotForm();
 if(typeof render==='function')render();
 overlay.remove();banner();
 });
}
window.GrowPlotNewUserPreview=function(){
 const auth=document.getElementById('authScreen');if(auth)auth.hidden=true;
 banner();window.scrollTo({top:0,behavior:'smooth'});
};
function addPreview(){
 const login=document.getElementById('loginView');if(!login||document.getElementById('growplotPreviewButton'))return;
 const b=document.createElement('button');b.id='growplotPreviewButton';b.type='button';b.textContent='Preview new-user home (demo)';
 b.style.cssText='display:block;margin:12px auto;border:1px solid #166534;border-radius:8px;padding:10px;background:transparent;color:#166534;cursor:pointer';
 b.onclick=window.GrowPlotNewUserPreview;login.appendChild(b);
}
addPreview();
})();