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
 action.style.cssText='display:inline-flex;align-items:center;justify-content:center;gap:6px;width:auto;min-height:34px;margin:8px 0 0;padding:6px 11px;border:1px solid #d3dfd6;border-radius:8px;background:#fff;color:#286647;font:inherit;font-size:13px;font-weight:600;cursor:pointer;box-shadow:none';
 action.innerHTML='<svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/><rect x="2" y="2" width="20" height="20" rx="4"/></svg><span>Add Plot</span>';
 action.addEventListener('click',openPlotSetup);
 // Keep the control unobtrusive at the lower-left of the plot/dashboard area.\n // Place within the Plot/Greenhouse selection panel, before the Home main buttons.
 const selectors=[...document.querySelectorAll('select')];
 const plotSelect=selectors.find(el=>/plot/i.test([el.id,el.name,el.getAttribute('aria-label')||''].join(' ')));
 const greenhouseSelect=selectors.find(el=>/greenhouse/i.test([el.id,el.name,el.getAttribute('aria-label')||''].join(' ')));
 const selector=greenhouseSelect||plotSelect;
 const selectorPanel=selector&&(selector.closest('.filter-panel,.selector-panel,.selection-panel,.dashboard-filters,.filter-bar,.filters,.card')||selector.parentElement?.parentElement);
 if(selectorPanel){selectorPanel.appendChild(action);}else{target.prepend(action);}
}
window.GrowPlotNewUserPreview=function(){const home=document.getElementById('homeBtn');if(home)home.click();showFirstPlotAction();};
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',showFirstPlotAction,{once:true});else showFirstPlotAction();
window.GrowPlotClearFirstPlotAction=function(){dismissLegacyPrompt();removeAction();};
})();
