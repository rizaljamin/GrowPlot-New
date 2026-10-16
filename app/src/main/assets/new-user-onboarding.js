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
 // The selector header is a two-column grid. Reserve a narrow third column for Add Plot.
 const selects=[...document.querySelectorAll('select')];
 const named=el=>[el.id,el.name,el.getAttribute('aria-label')||''].join(' ');
 const plot=selects.find(el=>/plot/i.test(named(el)));
 const greenhouse=selects.find(el=>/greenhouse/i.test(named(el)));
 if(plot&&greenhouse){
  const plotField=plot.parentElement;
  const greenhouseField=greenhouse.parentElement;
  const header=plotField&&greenhouseField&&plotField.parentElement===greenhouseField.parentElement?plotField.parentElement:null;
  if(header){
   header.style.display='grid';
   header.style.gridTemplateColumns='minmax(0,1.5fr) minmax(0,1fr) auto';
   header.style.alignItems='end';
   header.style.columnGap='10px';
   header.style.rowGap='0';
   plotField.style.minWidth='0';greenhouseField.style.minWidth='0';
   const buttonField=document.createElement('div');
   buttonField.id='growplotAddPlotField';
   buttonField.style.cssText='display:flex;align-items:end;justify-content:flex-end;min-width:max-content;padding-bottom:4px';
   buttonField.appendChild(action);
   header.appendChild(buttonField);
  }else{
   greenhouse.insertAdjacentElement('afterend',action);
  }
 }else{target.prepend(action);}

}
window.GrowPlotNewUserPreview=function(){const home=document.getElementById('homeBtn');if(home)home.click();showFirstPlotAction();};
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',showFirstPlotAction,{once:true});else showFirstPlotAction();
window.GrowPlotClearFirstPlotAction=function(){dismissLegacyPrompt();removeAction();};
})();
