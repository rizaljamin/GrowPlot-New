(function(){
'use strict';
if(window.GrowPlotAccountLoginInstalled)return;
window.GrowPlotAccountLoginInstalled=true;
const api=window.GrowPlotAccount;
const login=document.getElementById('loginForm'),create=document.getElementById('createForm');
const status=document.getElementById('authStatus');
// Firebase email/password UI. This changes the form only; backend migration follows.
function configureEmailFields(){
 for(const form of [login,create]){
  if(!form)continue;
  const field=form.querySelector('#loginIdentity, input[name="username"], input[name="email"], input[type="email"], input[type="text"]');
  if(!field)continue;
  field.type='email';field.setAttribute('inputmode','email');field.setAttribute('autocomplete','email');field.setAttribute('name','email');field.setAttribute('placeholder','Email address');field.setAttribute('aria-label','Email address');field.required=true;
  const label=field.id?form.querySelector('label[for="'+field.id+'"]'):null;
  if(label)label.textContent='Email address';
 }
}
configureEmailFields();
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
function userPage(){
 if(!currentUser){msg('Please log in first.');return;}
 const auth=document.getElementById('authScreen');if(auth)auth.hidden=true;
 let page=document.getElementById('growplotUserManagementPage');
 if(!page){page=document.createElement('section');page.id='growplotUserManagementPage';page.style.cssText='position:fixed;inset:0;z-index:99000;overflow:auto;background:#f5f8f5;padding:22px;color:#163b25;box-sizing:border-box';
 const back=document.createElement('button');back.type='button';back.textContent='← Back to GrowPlot';back.style.cssText='padding:12px;border-radius:8px;border:1px solid #568b66;background:white';back.onclick=()=>{page.hidden=true;goHome();};
 const heading=document.createElement('h2');heading.textContent=currentUser.role==='ADMIN'?'User Statistics':'My Account';
 const details=document.createElement('div');details.id='growplotUserManagementDetails';details.style.cssText='padding:16px;margin-top:14px;border:1px solid #c4d7c8;border-radius:12px;background:white;line-height:2';
 const logout=document.createElement('button');logout.type='button';logout.textContent='Log out';logout.style.cssText='display:block;margin-top:20px;padding:12px;border:1px solid #a55;border-radius:8px;background:white';logout.onclick=()=>{if(api&&api.logout)api.logout();currentUser=null;window.GrowPlotCurrentUser=null;page.remove();if(auth)auth.hidden=false;};
 page.append(back,heading,details,logout);document.body.appendChild(page);
 }
 page.hidden=false;const details=document.getElementById('growplotUserManagementDetails');
 if(currentUser.role==='ADMIN'){
   if(api&&api.configured()){details.textContent='Loading user statistics…';request('stats',{});}
   else {const n=Object.keys(localStorage).filter(k=>k.startsWith('growplot_test_account_')).length;details.textContent='Registered test users on this device: '+n+' (development only). Google Drive user statistics unavailable.';}
 }else details.textContent='Email: '+(currentUser.email||currentUser.username+' | Account type: Normal User';
}
window.GrowPlotOpenUserManagement=userPage;
document.addEventListener('click',function(e){
 const target=e.target&&e.target.closest?e.target.closest('button,a,[role="button"]'):null;
 if(!target||!currentUser)return;
 const id=(target.id||'').toLowerCase();
 const text=(target.textContent||'').trim().toLowerCase();
 const named=/^(user|users|my account|account|user management|user statistics)$/.test(text);
 const identified=/^(userbtn|btnuser|accountbtn|myaccountbtn|navuser|navaccount|userbutton)$/.test(id);
 if(named||identified){e.preventDefault();e.stopImmediatePropagation();userPage();}
},true);
function saveNotice(message,success){
 let el=document.getElementById('growplotSaveNotice');
 if(!el){el=document.createElement('div');el.id='growplotSaveNotice';el.setAttribute('role','status');el.style.cssText='position:fixed;bottom:18px;left:16px;right:16px;z-index:100001;padding:14px;border-radius:10px;box-shadow:0 3px 14px #0003;font-weight:600;text-align:center';document.body.appendChild(el);}
 el.style.background=success?'#d8f4df':'#ffe5df';el.style.color=success?'#164b2a':'#782319';el.textContent=message;
}
window.GrowPlotAccountResponse=function(response){
 if(!response.ok){msg(response.error||'Account request failed');if(response.action==='saveProfile')saveNotice('Google Drive save failed: '+(response.error||'Please retry.'),false);return;}
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
   const p=document.getElementById('growplotAdminStats');if(p)p.textContent='GrowPlot users: '+data.totalUsers+' | Standard users: '+data.standardUsers+' | Administrators: '+data.administrators+' | Logged in last 7 days: '+data.loggedInLast7Days;const d=document.getElementById('growplotUserManagementDetails');if(d)d.textContent='Total users: '+data.totalUsers+' | Standard users: '+data.standardUsers+' | Administrators: '+data.administrators+' | Logged in last 7 days: '+data.loggedInLast7Days;
 }else if(response.action==='saveProfile'){if(data.saved===true){msg('Data saved successfully to Google Drive.');saveNotice('Data saved successfully to Google Drive.',true);}else saveNotice('Save was not confirmed by Google Drive.',false); }
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
   if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(username)||suppliedPassword.length<8){msg('Enter a valid email address and a password of at least 8 characters.');return;}
   const key='growplot_test_account_'+username.toLowerCase();
   if(action==='register'){
     if(localStorage.getItem(key)){msg('Account already exists on this device.');return;}
     localStorage.setItem(key,JSON.stringify({password:suppliedPassword,plots:[],selectedPlotId:null}));
   }
   const record=JSON.parse(localStorage.getItem(key)||'null');
   if(!record||record.password!==suppliedPassword){msg('Incorrect email or password.');return;}
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
 msg(action==='login'?'Checking account…':'Creating account…');
 request(action,{email:username.trim(),username:username.trim(),password});
}
if(login)login.addEventListener('submit',e=>submit(e,'login'),true);
if(create)create.addEventListener('submit',e=>submit(e,'register'),true);
window.GrowPlotAdminVerified=function(){currentUser={username:'growplot_admin',role:'ADMIN',developmentOnly:true};window.GrowPlotCurrentUser=currentUser;goHome();demo();const home=document.getElementById('mainDashboard')||document.body;let panel=document.getElementById('growplotAdminStats');if(!panel){panel=document.createElement('section');panel.id='growplotAdminStats';home.prepend(panel);}const n=Object.keys(localStorage).filter(k=>k.startsWith('growplot_test_account_')).length;panel.textContent='Development user stats: '+n+' accounts on this device. Google Drive stats pending.';};
window.GrowPlotSaveCloudProfile=function(){
 if(!currentUser){saveNotice('Sign in before saving your user profile.',false);return;}if(currentUser.role==='ADMIN')return;
 if(!api||!api.configured()){saveNotice('Google Drive is not connected. Changes are NOT saved to Drive.',false);const key='growplot_test_account_'+currentUser.username.toLowerCase();const record=JSON.parse(localStorage.getItem(key)||'null');if(record){record.plots=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&!/demo/i.test(String(p.name||'')));record.selectedPlotId=typeof selectedPlotId==='undefined'?null:selectedPlotId;localStorage.setItem(key,JSON.stringify(record));}return;}
 const mine=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&p.id!=='plot-1'&&!/demo/i.test(String(p.name||'')));
 saveNotice('Saving to Google Drive…',false);request('saveProfile',{plots:mine,settings:{},selectedPlotId:typeof selectedPlotId==='undefined'?null:selectedPlotId});
};
const preview=document.getElementById('growplotPreviewButton');if(preview)preview.remove();
// Development build permits demo administrator access; no false cloud-authentication claims.
})();