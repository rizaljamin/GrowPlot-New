/* GrowPlot Home plot selector. Only authenticated Firebase-authorized plots are displayed. */
(function(){'use strict';
function syncAuthorizedHomePlots(){
 const select=document.getElementById('plot');if(!select)return;
 const user=window.GrowPlotCurrentUser,uid=String(user?.uid||user?.id||'');
 const profile=window.GrowPlotCloudProfile,power=!!(uid&&profile?.powerUser===true);
 const seen=new Set(['plot-1']);
 const safe=(uid&&Array.isArray(profile?.plots)?profile.plots:[]).filter(p=>{
  if(!p||!p.id||seen.has(String(p.id))||(!power&&String(p.ownerUserId||'')!==uid))return false;
  seen.add(String(p.id));return true;
 }).sort((a,b)=>String(a.id).localeCompare(String(b.id),undefined,{numeric:true,sensitivity:'base'}));
 const choices=[{id:'plot-1',name:'Demo Plot'},...safe];
 const labels=choices.map(p=>p.id==='plot-1'?'plot-1 - Demo Plot':String(p.id)+' - '+String(p.name||p.plotName||p.id)+(power?' ('+String(p.ownerEmail||p.ownerName||p.ownerUserId||'Unknown owner')+')':''));
 const old=select.value,previousSelected=typeof selectedPlotId!=='undefined'?selectedPlotId:old,desired=choices.some(p=>String(p.id)===old)?old:'plot-1';
 if(select.options.length!==choices.length||Array.from(select.options).some((o,i)=>o.value!==String(choices[i].id)||o.textContent!==labels[i])){
  select.replaceChildren();choices.forEach((p,i)=>{const o=document.createElement('option');o.value=String(p.id);o.textContent=labels[i];select.appendChild(o);});
 }
 select.value=desired;
 if(typeof selectedPlotId!=='undefined')selectedPlotId=desired;
 if(old!==desired||previousSelected!==desired){select.dispatchEvent(new Event('change',{bubbles:true}));window.GrowPlotRefreshHomeStreams?.();}
}

window.GrowPlotPlotDropdownSync=syncAuthorizedHomePlots;
window.dispatchEvent(new Event('growplot-plot-dropdown-ready'));
})();
