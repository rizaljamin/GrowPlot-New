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
 action.style.cssText='display:inline-flex;align-items:center;justify-content:center;gap:6px;flex:0 0 auto;width:auto;min-height:36px;margin:0;padding:7px 12px;border:0;border-radius:8px;background:#286647;color:#fff;font:inherit;font-size:13px;font-weight:700;cursor:pointer;box-shadow:none';
 action.innerHTML='<svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/><rect x="2" y="2" width="20" height="20" rx="4"/></svg><span>Add Plot</span>';
 action.addEventListener('click',openPlotSetup);
 // Keep Add Plot on the SAME row as the Plot and Greenhouse dropdowns.
 const selects=[...document.querySelectorAll('select')];
 const named=el=>[el.id,el.name,el.getAttribute('aria-label')||''].join(' ');
 const plot=selects.find(el=>/plot/i.test(named(el)));
 const greenhouse=selects.find(el=>/greenhouse/i.test(named(el)));
 const selector=greenhouse||plot;
 if(selector){
  const row=plot&&greenhouse&&plot.parentElement===greenhouse.parentElement?plot.parentElement:selector.parentElement;
  row.style.display='flex';row.style.alignItems='center';row.style.flexWrap='wrap';row.style.gap='8px';
  row.appendChild(action);
 }else{target.prepend(action);}

