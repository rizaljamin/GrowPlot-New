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
 const settings=document.getElementById('settingsBtn');
 if(settings)settings.click();
 requestAnimationFrame(()=>{
  const section=document.getElementById('plotManagementSection');
  if(section){section.open=true;section.scrollIntoView({behavior:'smooth',block:'start'});}
 });
}
function removeAction(){if(action){action.remove();action=null;}}
function showFirstPlotAction(){
 dismissLegacyPrompt();removeAction();
 const home=document.getElementById('homeBtn');if(home)home.click();
 const dashboard=document.getElementById('mainDashboard');
 const target=dashboard||document.getElementById('mainContent')||document.body;
 action=document.createElement('button');action.type='button';action.id='growplotAddPlotBtn';
 action.style.cssText='display:flex;align-items:center;justify-content:center;gap:10px;width:min(100%,340px);margin:20px auto;padding:15px 20px;border:0;border-radius:12px;background:#286647;color:white;font:inherit;font-weight:700;cursor:pointer;box-shadow:0 3px 12px #0002';
 action.innerHTML='<svg aria-hidden="true" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/><rect x="2" y="2" width="20" height="20" rx="4"/></svg><span>Add Plot</span>';
 action.addEventListener('click',openPlotSetup);
 target.prepend(action);
}
window.GrowPlotNewUserPreview=showFirstPlotAction;
window.GrowPlotClearFirstPlotAction=function(){dismissLegacyPrompt();removeAction();};
})();
