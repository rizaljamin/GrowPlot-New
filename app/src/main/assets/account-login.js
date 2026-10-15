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
  const field=form.querySelector('input[name="email"], #loginIdentity, input[type="email"]');
  if(!field)continue;
  field.type='email';field.setAttribute('inputmode','email');field.setAttribute('autocomplete','email');field.setAttribute('name','email');field.setAttribute('placeholder','Email address');field.setAttribute('aria-label','Email address');field.required=true;
  const label=field.id?form.querySelector('label[for="'+field.id+'"]'):null;
  if(label)label.textContent='Email address';
 }
}
configureEmailFields();
let currentUser=null;
let saveProfileCallback=null;
let passwordResetButton=null;
let resetFeedbackTarget=null;
let registrationResetButton=null;
function msg(t){if(status)status.textContent=t;}
function request(action,payload){api.request(action,JSON.stringify(payload||{}));}
function registrationStatus(){
 const target=document.getElementById('createStatus')||status;
 const submit=create&&create.querySelector('button[type="submit"],input[type="submit"]');
 if(create&&target&&submit&&submit.nextElementSibling!==target)submit.insertAdjacentElement('afterend',target);
 return target;
}
function showRegistrationReset(){
 const target=registrationStatus();if(!target)return;
 target.textContent='This ID is already a registered member. Click Forgot password to update new password.';
 if(registrationResetButton)registrationResetButton.remove();
 registrationResetButton=document.createElement('button');registrationResetButton.type='button';registrationResetButton.id='registrationForgotPasswordBtn';registrationResetButton.textContent='Forgot password';
 registrationResetButton.style.cssText='display:block;margin:8px 0 0;padding:4px 0;border:0;background:transparent;color:#286647;font:inherit;font-weight:650;text-decoration:underline;cursor:pointer';
 target.insertAdjacentElement('afterend',registrationResetButton);
 registrationResetButton.addEventListener('click',()=>{
  const email=create&&create.querySelector('input[name="email"]');
  if(!email||!email.value.trim()||!email.checkValidity()){target.textContent='Enter a valid email address above first.';if(email)email.focus();return;}
  if(!api||!api.configured()){target.textContent='Firebase is not configured.';return;}
  resetFeedbackTarget=target;registrationResetButton.disabled=true;registrationResetButton.textContent='Sending reset link…';target.textContent='Sending password reset email…';
  request('passwordReset',{email:email.value.trim()});
 });
}
function addPasswordVisibilityToggles(){
 for(const form of [login,create]){
  if(!form)continue;
  for(const field of form.querySelectorAll('input[type="password"]')){
   const group=field.closest('.auth-field')||field.parentElement;
   if(!group||group.dataset.growplotPasswordToggle==='true')continue;
   if(!field.id)field.id='growplot-'+(form.id||'account')+'-'+(field.name||'password');
   const toggle=document.createElement('button');toggle.type='button';toggle.className='growplot-password-toggle';
   const eyeOpen='<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-6 10-6 10 6 10 6-3.6 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
   const eyeClosed='<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-6 10-6c2.1 0 3.9.6 5.4 1.4M22 12s-1.1 1.9-3.2 3.7A10 10 0 0 1 12 18c-6.4 0-10-6-10-6"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2M3 3l18 18"/></svg>';
   toggle.innerHTML=eyeOpen;toggle.title='Show password';toggle.setAttribute('aria-label','Show '+(field.name==='confirmPassword'?'confirm password':'password'));
   toggle.setAttribute('aria-controls',field.id);toggle.setAttribute('aria-pressed','false');
   toggle.style.cssText='display:flex;align-items:center;justify-content:center;width:36px;height:32px;margin:0 0 8px auto;padding:4px;border:0;border-radius:6px;background:transparent;color:#286647;cursor:pointer';
   toggle.addEventListener('click',()=>{const visible=field.type==='password';field.type=visible?'text':'password';toggle.innerHTML=visible?eyeClosed:eyeOpen;toggle.title=(visible?'Hide':'Show')+' password';toggle.setAttribute('aria-label',(visible?'Hide ':'Show ')+(field.name==='confirmPassword'?'confirm password':'password'));toggle.setAttribute('aria-pressed',String(visible));});
   group.dataset.growplotPasswordToggle='true';group.insertAdjacentElement('afterend',toggle);
  }
 }
}
function installPasswordReset(){
 if(!login)return;
 passwordResetButton=document.createElement('button');passwordResetButton.type='button';passwordResetButton.id='forgotPasswordBtn';passwordResetButton.textContent='Forgot password?';
 passwordResetButton.style.cssText='display:block;margin:0 0 12px auto;padding:4px 0;border:0;background:transparent;color:#286647;font:inherit;font-weight:650;text-decoration:underline;cursor:pointer';
 const submit=login.querySelector('button[type="submit"],input[type="submit"]');
 if(submit)login.insertBefore(passwordResetButton,submit);else login.appendChild(passwordResetButton);
 passwordResetButton.addEventListener('click',()=>{
  const email=login.querySelector('input[name="email"]');
  if(!email||!email.value.trim()||!email.checkValidity()){msg('Enter a valid email address above first.');if(email)email.focus();return;}
  if(!api||!api.configured()){msg('Firebase is not configured.');return;}
  resetFeedbackTarget=status;passwordResetButton.disabled=true;passwordResetButton.textContent='Sending reset link…';msg('Sending password reset email…');
  request('passwordReset',{email:email.value.trim()});
 });
}
addPasswordVisibilityToggles();installPasswordReset();
function goHome(){const auth=document.getElementById('authScreen');if(auth)auth.hidden=true;if(typeof returnHome==='function')returnHome();else if(typeof render==='function')render();}
function demo(){if(typeof plots!=='undefined'&&Array.isArray(plots)){const p=plots.find(p=>/demo/i.test(String(p.name||'')+' '+String(p.id||'')))||plots[0];if(p){selectedPlotId=p.id;if(typeof fillPlotSelectors==='function')fillPlotSelectors();if(typeof render==='function')render();}}}
window.GrowPlotShowDemoHome=function(){demo();goHome();};
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
 }else{
   const formatDate=value=>{const date=Number.isFinite(Number(value))?new Date(Number(value)):new Date(value);return Number.isNaN(date.getTime())?'Not available':new Intl.DateTimeFormat(undefined,{dateStyle:'medium',timeStyle:'short'}).format(date);};
   details.innerHTML='';
   const rows=[['Email',currentUser.email||currentUser.username||'Not available'],['Account type','Normal User'],['Registration date',formatDate(currentUser.registrationDate)],['Last login',formatDate(currentUser.lastLoginDate)],['Payment method','Not configured']];
   for(const [label,value] of rows){const row=document.createElement('p');row.style.margin='0 0 8px';const name=document.createElement('strong');name.textContent=label+': ';row.append(name,document.createTextNode(value));details.appendChild(row);}
 }
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
 if(response.action==='passwordReset'){
  if(passwordResetButton){passwordResetButton.disabled=false;passwordResetButton.textContent='Forgot password?';}
  const target=resetFeedbackTarget||status;resetFeedbackTarget=null;
  if(registrationResetButton){registrationResetButton.disabled=false;registrationResetButton.textContent='Forgot password';}
  if(target)target.textContent=response.ok?'If a GrowPlot account matches that address, a reset email will arrive shortly. Check your inbox and spam folder.':(response.error||'Could not send a reset email. Please check your connection and try again.');
  return;
 }
 if(!response.ok){if(response.action==='googleSignIn'){const createView=document.getElementById('createView');const target=createView&&!createView.hidden?document.getElementById('createStatus'):status;if(target)target.textContent=response.error||'Google sign-in failed.';}else if(response.action==='register'&&response.data&&response.data.duplicateEmail){showRegistrationReset();}else if(response.action==='register'){const target=registrationStatus();if(target)target.textContent=response.error||'Account request failed.';}else if(response.action==='profile'&&currentUser){window.GrowPlotCloudProfile=null;saveNotice('Signed in, but your Firebase cloud profile could not be loaded: '+(response.error||'Please retry.'),false);goHome();return;}else msg((response.action==='profile'?'Firebase profile could not be loaded: ':'')+(response.error||'Account request failed'));if(response.action==='saveProfile'){const callback=saveProfileCallback;saveProfileCallback=null;if(callback)callback(false,response.error||'Firebase save failed.');}if(response.action==='profile'){const authScreen=document.getElementById('authScreen');if(authScreen)authScreen.hidden=false;currentUser=null;window.GrowPlotCurrentUser=null;}if(response.action==='saveProfile')saveNotice('Firebase save failed: '+(response.error||'Please retry.'),false);return;}
 const data=response.data||{};
 if(response.action==='accountWarning'){saveNotice(data.warning||'Cloud profile update failed. Your sign-in is still active.',false);return;}
 if(response.action==='login'||response.action==='register'||response.action==='googleSignIn'){
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
 }else if(response.action==='saveProfile'){const saved=data.saved===true;if(saved){msg('Data saved successfully to Firebase.');saveNotice('Data saved successfully to Firebase.',true);}else saveNotice('Save was not confirmed by Firebase.',false);const callback=saveProfileCallback;saveProfileCallback=null;if(callback)callback(saved,response.error||''); }
};
function submit(e,action){
 e.preventDefault();e.stopImmediatePropagation();
 if(!api||!api.configured()){msg('Firebase is not configured.');return;}
 const form=e.currentTarget;
 const email=(form.querySelector('input[name="email"], #loginIdentity, input[type="email"]')||{}).value||'';
 if(action==='register'&&form.elements.password.value!==form.elements.confirmPassword.value){const createStatus=document.getElementById('createStatus');if(createStatus)createStatus.textContent='The passwords do not match.';return;}
 const password=(form.querySelector('input[name="password"],input[type="password"]')||{}).value||'';
 msg(action==='login'?'Signing in with Firebase…':'Registering with Firebase…');
 request(action,{email:email.trim(),password});
}
if(login)login.addEventListener('submit',e=>submit(e,'login'),true);
if(create)create.addEventListener('submit',e=>submit(e,'register'),true);
function beginGoogleSignIn(){if(!api||!api.configured()){msg('Firebase is not configured.');return;}const createView=document.getElementById('createView');const target=createView&&!createView.hidden?document.getElementById('createStatus'):status;if(target)target.textContent='Opening Google sign-in…';if(api.startGoogleSignIn)api.startGoogleSignIn();else if(target)target.textContent='Google sign-in is unavailable in this build.';}
document.addEventListener('click',function(e){const button=e.target&&e.target.closest?e.target.closest('#googleLoginBtn,#googleRegisterBtn'):null;if(!button)return;e.preventDefault();e.stopImmediatePropagation();beginGoogleSignIn();},true);
if(!login||!create)msg('GrowPlot login setup error: expected login and registration forms were not found.');
window.GrowPlotAdminVerified=function(){currentUser={username:'growplot_admin',role:'ADMIN',developmentOnly:true};window.GrowPlotCurrentUser=currentUser;goHome();demo();const home=document.getElementById('mainDashboard')||document.body;let panel=document.getElementById('growplotAdminStats');if(!panel){panel=document.createElement('section');panel.id='growplotAdminStats';home.prepend(panel);}const n=Object.keys(localStorage).filter(k=>k.startsWith('growplot_test_account_')).length;panel.textContent='Development user stats: '+n+' accounts on this device. Google Drive stats pending.';};
window.GrowPlotSaveCloudProfile=function(callback){
 if(!currentUser){saveNotice('Sign in before saving your user profile.',false);if(callback)callback(false,'Sign in first.');return;}if(currentUser.role==='ADMIN'){if(callback)callback(true,'');return;}
 if(!api||!api.configured()){saveNotice('Firebase unavailable. Data was not saved.',false);if(callback)callback(false,'Firebase is unavailable.');return;}
 const mine=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&p.id!=='plot-1'&&!/demo/i.test(String(p.name||'')));
 saveProfileCallback=callback||null;saveNotice('Saving to Firebase…',false);request('saveProfile',{plots:mine,settings:{},selectedPlotId:typeof selectedPlotId==='undefined'?null:selectedPlotId});
};
const preview=document.getElementById('growplotPreviewButton');if(preview)preview.remove();
// Development build permits demo administrator access; no false cloud-authentication claims.
})();
