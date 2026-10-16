(function(){
'use strict';
let prompt=null;
function hidePrompt(){
 if(!prompt)return;
 if(typeof prompt.close==='function'&&prompt.open)prompt.close();
 prompt.remove();
 prompt=null;
}
function openPlotManagement(){
 hidePrompt();
 const settings=document.getElementById('settingsBtn');
 if(settings)settings.click();
 requestAnimationFrame(()=>{
  const section=document.getElementById('plotManagementSection');
  if(section){section.open=true;section.scrollIntoView({behavior:'smooth',block:'start'});}
 });
}
function showDemoHome(){
 hidePrompt();
 if(typeof window.GrowPlotShowDemoHome==='function')window.GrowPlotShowDemoHome();
 else {const home=document.getElementById('homeBtn');if(home)home.click();}
}
function showHomeBehindPrompt(){
 const home=document.getElementById('homeBtn');
 if(home){home.click();return;}
 const modal=document.getElementById('aiModal');
 if(modal)modal.classList.remove('open','health-open','farm-open');
 const health=document.getElementById('healthPanel');
 if(health)health.hidden=true;
 const dashboard=document.getElementById('mainDashboard');
 if(dashboard)dashboard.hidden=false;
}
function showFirstPlotChoice(){
 showHomeBehindPrompt();
 if(prompt)hidePrompt();
 prompt=document.createElement('dialog');prompt.id='growplotFirstPlotPrompt';prompt.setAttribute('aria-labelledby','growplotFirstPlotTitle');
 prompt.style.cssText='position:fixed;inset:0;z-index:2147483647;width:100%;height:100%;max-width:none;max-height:none;margin:0;border:0;align-items:center;justify-content:center;padding:20px;background:#10231877;box-sizing:border-box;color:inherit';
 const card=document.createElement('section');card.style.cssText='width:min(100%,360px);padding:22px;border-radius:16px;background:#fff;color:#173b25;box-shadow:0 12px 36px #0004;box-sizing:border-box;text-align:center';
 const title=document.createElement('h2');title.id='growplotFirstPlotTitle';title.textContent='Create your first plot';title.style.margin='0 0 8px';
 const intro=document.createElement('p');intro.textContent='Your account is ready. Choose where you’d like to go next.';intro.style.cssText='margin:0 0 18px;color:#536358;line-height:1.45';
 const create=document.createElement('button');create.type='button';create.textContent='Create Plot';create.style.cssText='display:block;width:100%;padding:12px;border:0;border-radius:9px;background:#286647;color:white;font:inherit;font-weight:700;cursor:pointer';create.addEventListener('click',openPlotManagement);
 const home=document.createElement('button');home.type='button';home.textContent='Home';home.style.cssText='display:block;width:100%;margin-top:9px;padding:11px;border:1px solid #b9c8bb;border-radius:9px;background:white;color:#173b25;font:inherit;font-weight:650;cursor:pointer';home.addEventListener('click',showDemoHome);
 card.append(title,intro,create,home);prompt.appendChild(card);document.body.appendChild(prompt);
 if(typeof prompt.showModal==='function')prompt.showModal();else{prompt.setAttribute('role','dialog');prompt.setAttribute('aria-modal','true');prompt.style.display='flex';}
 create.focus();
}
window.GrowPlotNewUserPreview=showFirstPlotChoice;
})();
