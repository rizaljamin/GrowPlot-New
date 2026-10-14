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
 if(!api||!api.configured()){
   const form=e.currentTarget;
   const username=((form.querySelector('#loginIdentity, input[name="username"], input[name="email"], input[type="email"], input[type="text"]')||{}).value||'').trim();
   const suppliedPassword=(form.querySelector('input[name="password"],input[type="password"]')||{}).value||'';
   if(action==='login'&&username.toLowerCase()==='growplot_admin'){
     if(!window.GrowPlotBridge||!window.GrowPlotBridge.loginAdminOtpDemo){msg('Administrator verifier unavailable');return;}
     window.GrowPlotBridge.loginAdminOtpDemo(suppliedPassword);
     return;
   }
   if(!/^[a-zA-Z0-9_.-]{3,64}$/.test(username)||suppliedPassword.length<8){msg('Username must be at least 3 characters and password at least 8.');return;}
   const key='growplot_test_account_'+username.toLowerCase();
   if(action==='register'){
     if(localStorage.getItem(key)){msg('Account already exists on this device.');return;}
     localStorage.setItem(key,JSON.stringify({password:suppliedPassword,plots:[],selectedPlotId:null}));
   }
   const record=JSON.parse(localStorage.getItem(key)||'null');
   if(!record||record.password!==suppliedPassword){msg('Incorrect username or password.');return;}
   currentUser={username,role:'USER',developmentOnly:true};window.GrowPlotCurrentUser=currentUser;
   goHome();
   if(record.plots&&record.plots.length){
     if(typeof plots!=='undefined'&&Array.isArray(plots)){for(const p of record.plots)if(!plots.some(x=>x.id===p.id))plots.push(p);selectedPlotId=record.selectedPlotId||record.plots[0].id;if(typeof fillPlotSelectors==='function')fillPlotSelectors();if(typeof render==='function')render();}
   }else if(typeof window.GrowPlotNewUserPreview==='function')window.GrowPlotNewUserPreview();
   msg('Development account: stored on this device only; Google Drive sync pending.');
   return;
 }
 const form=e.currentTarget;
 const username=(form.querySelector('#loginIdentity, input[name="username"], input[name="email"], input[type="email"], input[type="text"]')||{}).value||'';
 const password=(form.querySelector('input[name="password"],input[type="password"]')||{}).value||'';
 msg(action==='login'?'Checking account on Google Drive…':'Creating account on Google Drive…');
 request(action,{username:username.trim(),password});
}
if(login)login.addEventListener('submit',e=>submit(e,'login'),true);
if(create)create.addEventListener('submit',e=>submit(e,'register'),true);
window.GrowPlotAdminVerified=function(){currentUser={username:'growplot_admin',role:'ADMIN',developmentOnly:true};window.GrowPlotCurrentUser=currentUser;goHome();demo();const home=document.getElementById('mainDashboard')||document.body;let panel=document.getElementById('growplotAdminStats');if(!panel){panel=document.createElement('section');panel.id='growplotAdminStats';home.prepend(panel);}const n=Object.keys(localStorage).filter(k=>k.startsWith('growplot_test_account_')).length;panel.textContent='Development user stats: '+n+' accounts on this device. Google Drive stats pending.';};
window.GrowPlotSaveCloudProfile=function(){
 if(!currentUser||currentUser.role==='ADMIN')return;
 if(!api||!api.configured()){const key='growplot_test_account_'+currentUser.username.toLowerCase();const record=JSON.parse(localStorage.getItem(key)||'null');if(record){record.plots=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&!/demo/i.test(String(p.name||'')));record.selectedPlotId=typeof selectedPlotId==='undefined'?null:selectedPlotId;localStorage.setItem(key,JSON.stringify(record));}return;}
 const mine=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&p.id!=='plot-1'&&!/demo/i.test(String(p.name||'')));
 request('saveProfile',{plots:mine,settings:{},selectedPlotId:typeof selectedPlotId==='undefined'?null:selectedPlotId});
};
const preview=document.getElementById('growplotPreviewButton');if(preview)preview.remove();
// Development build permits demo administrator access; no false cloud-authentication claims.
})();