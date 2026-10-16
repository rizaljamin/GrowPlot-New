(function(){
'use strict';
// New users land on Home; never stack a second modal over Settings.
let action=null;
function dismissLegacyPrompt(){
 const old=document.getElementById('growplotFirstPlotPrompt');
 if(old){if(old.open&&typeof old.close==='function')old.close();old.remove();}
}
function openPlotSetup(){
 dismissLegacyPrompt();
 // Navigate to Manage Farm's Plot tab, not the full Settings screen.
 const manageFarm=document.getElementById('manageFarmBtn')||document.querySelector('[data-tab="manageFarm"]');
 if(manageFarm)manageFarm.click();
 if(typeof window.GrowPlotOpenPlotManagement==='function'){
  window.GrowPlotOpenPlotManagement();return;
 }
 // Existing Manage Farm tabs expose a Plot button; use it after rendering.
 requestAnimationFrame(()=>{
  const buttons=[...document.querySelectorAll('button')];
  const plotTab=buttons.find(button=>button.textContent.trim()==='Plot'&&button.closest('#manageFarmContent, #manageFarmSection, .manage-farm-tabs'));
  if(plotTab)plotTab.click();
 });
}
function removeAction(){if(action){action.remove();action=null;}}
function showFirstPlotAction(){
 dismissLegacyPrompt();
 if(action&&action.isConnected)return;
 removeAction();
 const dashboard=document.getElementById('mainDashboard');
 const target=dashboard||document.getElementById('mainContent')||document.body;
 action=document.createElement('button');action.type='button';action.id='growplotAddPlotBtn';
 action.style.cssText='position:relative;display:inline-flex;align-items:center;justify-content:center;flex:0 0 auto;width:auto;min-height:28px;margin:0;padding:5px 18px 5px 8px;border:0;border-radius:6px;background:#286647;color:#fff;font:inherit;font-size:11px;font-weight:700;cursor:pointer;box-shadow:none';
 action.innerHTML='<span>Add Plot</span><span aria-hidden="true" style="position:absolute;right:4px;bottom:2px;font-size:12px;line-height:1">+</span>';
 action.addEventListener('click',openPlotSetup);
 // Home header uses .selectors with #plot and #gh; insert as its third grid cell.
 // Build trigger: verify the right-aligned Add Plot layout on ARM64.
 const plot=document.getElementById('plot');
 const greenhouse=document.getElementById('gh');
 const row=plot&&greenhouse&&plot.closest('.selectors');
 if(row&&row===greenhouse.closest('.selectors')){
  row.style.gridTemplateColumns='minmax(0,1.5fr) minmax(0,1fr) max-content';
  row.style.alignItems='end';
  row.style.columnGap='10px';
  row.style.width='100%';
  const field=document.createElement('div');
  field.id='growplotAddPlotField';
  field.style.cssText='display:flex;align-items:flex-end;justify-content:flex-end;min-width:max-content';
  field.appendChild(action);
  row.appendChild(field);
 }else{target.prepend(action);}

}
window.GrowPlotNewUserPreview=function(){const home=document.getElementById('homeBtn');if(home)home.click();showFirstPlotAction();};
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',showFirstPlotAction,{once:true});else showFirstPlotAction();
window.GrowPlotClearFirstPlotAction=function(){dismissLegacyPrompt();removeAction();};
})();
