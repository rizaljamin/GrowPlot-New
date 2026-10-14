(function(){
'use strict';
if(window.GrowPlotAccountLoginInstalled)return;
window.GrowPlotAccountLoginInstalled=true;
const api=window.GrowPlotAccount;
const login=document.getElementById('loginForm'),create=document.getElementById('createForm');
const status=document.getElementById('authStatus');
let currentUser=null;
function msg(t){if(status)status.textContent=t;}
function request(action,payload){api.request(action,JSON.stringify(payload||{}));}
function goHome(){const auth=document.getElementById('authScreen');if(auth)auth.hidden=true;if(typeof returnHome==='function')returnHome();else if(typeof render==='function')render();}
function demo(){if(typeof plots!=='undefined'&&Array.isArray(plots)){const p=plots.find(p=>/demo/i.test(String(p.name||'')+' '+String(p.id||'')))||plots[0];if(p){selectedPlotId=p.id;if(typeof fillPlotSelectors==='function')fillPlotSelectors();if(typeof render==='function')render();}}}
function adminStats(){
 let panel=document.getElementById('growplotAdminStats');
 if(!panel){panel=document.createElement('section');panel.id='growplotAdminStats';panel.style.cssText='margin:12px;padding:12px;border:1px solid #aac;border-radius:12px;background:#f7faf7';const home=document.getElementById('mainDashboard');(home||document.body).prepend(panel);}
 panel.textContent='Loading user statistics…';request('stats',{});
}
window.GrowPlotAccountResponse=function(response){
 if(!response.ok){msg(response.error||'Account request failed');return;}
 const data=response.data||{};
 if(response.action==='login'||response.action==='register'){
   currentUser=data.user;window.GrowPlotCurrentUser=currentUser;
   if(!currentUser){msg('Invalid account response');return;}
   if(currentUser.role==='ADMIN'){goHome();demo();adminStats();}
   else {request('profile',{});}
 }else if(response.action==='profile'){
   window.GrowPlotCloudProfile=data;
   // Never replace the Demo Plot with a stale device-local user record.
   if(Array.isArray(data.plots)&&data.plots.length){
     if(typeof plots!=='undefined'&&Array.isArray(plots)){
       for(const p of data.plots){if(p&&p.id&&!plots.some(x=>x.id===p.id))plots.push(p);}
       selectedPlotId=data.selectedPlotId||data.plots[0].id;
       if(typeof fillPlotSelectors==='function')fillPlotSelectors();
     }
     goHome();
   }else{goHome();if(typeof window.GrowPlotNewUserPreview==='function')window.GrowPlotNewUserPreview();}
 }else if(response.action==='stats'){
   const p=document.getElementById('growplotAdminStats');if(p)p.textContent='GrowPlot users: '+data.totalUsers+' | Standard users: '+data.standardUsers+' | Administrators: '+data.administrators+' | Logged in last 7 days: '+data.loggedInLast7Days;
 }else if(response.action==='saveProfile'){msg('Plot profile saved to Google Drive.');}
};
function submit(e,action){
 e.preventDefault();e.stopImmediatePropagation();
 if(!api||!api.configured()){msg('Cloud account server not configured in this APK. Login is unavailable.');return;}
 const form=e.currentTarget;
 const username=(form.querySelector('#loginIdentity, input[name="username"], input[name="email"], input[type="email"], input[type="text"]')||{}).value||'';
 const password=(form.querySelector('input[name="password"],input[type="password"]')||{}).value||'';
 msg(action==='login'?'Checking account on Google Drive…':'Creating account on Google Drive…');
 request(action,{username:username.trim(),password});
}
if(login)login.addEventListener('submit',e=>submit(e,'login'),true);
if(create)create.addEventListener('submit',e=>submit(e,'register'),true);
window.GrowPlotSaveCloudProfile=function(){
 if(!currentUser||currentUser.role==='ADMIN')return;
 const mine=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&p.id!=='plot-1'&&!/demo/i.test(String(p.name||'')));
 request('saveProfile',{plots:mine,settings:{},selectedPlotId:typeof selectedPlotId==='undefined'?null:selectedPlotId});
};
const preview=document.getElementById('growplotPreviewButton');if(preview)preview.remove();
if(!api||!api.configured())msg('Cloud login requires an HTTPS account-service endpoint configured in the APK.');
})();