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
   const wrapper=document.createElement('span');wrapper.className='growplot-password-field';wrapper.style.cssText='position:relative;display:block;margin-top:6px';
   field.style.marginTop='0';field.style.paddingRight='48px';field.parentNode.insertBefore(wrapper,field);wrapper.appendChild(field);
   const toggle=document.createElement('button');toggle.type='button';toggle.className='growplot-password-toggle';
   const eyeOpen='<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-6 10-6 10 6 10 6-3.6 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
   const eyeClosed='<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-6 10-6c2.1 0 3.9.6 5.4 1.4M22 12s-1.1 1.9-3.2 3.7A10 10 0 0 1 12 18c-6.4 0-10-6-10-6"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2M3 3l18 18"/></svg>';
   toggle.innerHTML=eyeOpen;toggle.title='Show password';toggle.setAttribute('aria-label','Show '+(field.name==='confirmPassword'?'confirm password':'password'));
   toggle.setAttribute('aria-controls',field.id);toggle.setAttribute('aria-pressed','false');
   toggle.style.cssText='position:absolute;right:5px;top:50%;transform:translateY(-50%);display:flex;align-items:center;justify-content:center;width:36px;height:36px;margin:0;padding:4px;border:0;border-radius:6px;background:transparent;color:#286647;cursor:pointer';
   toggle.addEventListener('click',()=>{const visible=field.type==='password';field.type=visible?'text':'password';toggle.innerHTML=visible?eyeClosed:eyeOpen;toggle.title=(visible?'Hide':'Show')+' password';toggle.setAttribute('aria-label',(visible?'Hide ':'Show ')+(field.name==='confirmPassword'?'confirm password':'password'));toggle.setAttribute('aria-pressed',String(visible));});
   group.dataset.growplotPasswordToggle='true';wrapper.appendChild(toggle);
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
const adminTestLoginButton=document.getElementById('adminTestLoginBtn');
if(adminTestLoginButton){adminTestLoginButton.textContent='Login as Power User';adminTestLoginButton.hidden=!(api&&api.isDevelopmentBuild&&api.isDevelopmentBuild());}
if(adminTestLoginButton)adminTestLoginButton.addEventListener('click',function(){
 if(!api||!api.verifyLocalAdminTestLogin||!api.verifyLocalAdminTestLogin()){msg('Admin test login is unavailable in this build.');return;}
 msg('Signed in to the local development admin session. Firebase was not used.');
 if(typeof window.GrowPlotAdminVerified==='function')window.GrowPlotAdminVerified();
});
function goHome(){setTimeout(()=>window.GrowPlotRefreshHomeStreams?.(),300);const oldPage=document.getElementById('growplotUserManagementPage');if(oldPage)oldPage.remove();const auth=document.getElementById('authScreen');if(auth)auth.hidden=true;if(typeof returnHome==='function')returnHome();else if(typeof render==='function')render();}
// Owner-scoped Home dropdown: only the Demo Plot plus Firebase-authorized plots.
// Plot dropdown logic is loaded separately; show Demo only until the module is ready.
function syncAuthorizedHomePlots(){
 const select=document.getElementById('plot');if(!select)return;
 const user=window.GrowPlotCurrentUser,uid=String(user?.uid||user?.id||'');
 const profile=window.GrowPlotCloudProfile,power=!!(uid&&profile?.powerUser===true);
 const keyOf=p=>p.id==='plot-1'?'demo:plot-1':String(p.ownerUserId||p.ownerUid||uid)+':'+String(p.id);
 const seen=new Set(['demo:plot-1']);
 const safe=(uid&&Array.isArray(profile?.plots)?profile.plots:[]).filter(p=>{
  if(!p||!p.id)return false;
  const key=keyOf(p);if(seen.has(key))return false;seen.add(key);return true;
 }).sort((a,b)=>keyOf(a).localeCompare(keyOf(b),undefined,{numeric:true,sensitivity:'base'}));
 const choices=[{id:'plot-1',name:'Demo Plot'},...safe];
 const labels=choices.map(p=>p.id==='plot-1'?'plot-1 - Demo Plot':String(p.id)+' - '+String(p.name||p.plotName||p.id)+' ('+String(p.ownerEmail||p.ownerName||p.ownerDisplayName||p.ownerUserId||'Unknown owner')+')');
 const old=select.value;
 const currentKey=select.selectedOptions[0]?.dataset.plotKey||'';
 const desired=choices.some(p=>keyOf(p)===currentKey)?currentKey:keyOf(choices.find(p=>p.id===old)||choices[0]);
 const changed=select.options.length!==choices.length||Array.from(select.options).some((o,i)=>o.value!==String(choices[i].id)||o.dataset.plotKey!==keyOf(choices[i])||o.textContent!==labels[i]);
 if(changed){
  select.replaceChildren();choices.forEach((p,i)=>{const o=document.createElement('option');o.value=String(p.id);o.dataset.plotKey=keyOf(p);o.textContent=labels[i];select.appendChild(o);});
 }
 const index=choices.findIndex(p=>keyOf(p)===desired);
 if(index>=0)select.selectedIndex=index;
 window.GrowPlotSelectedOwnerPlotKey=desired;
 window.GrowPlotSelectedPlotOwnerUid=choices[index]?.ownerUserId||choices[index]?.ownerUid||uid;
 renderHomePlotTree();
 if(typeof selectedPlotId!=='undefined'&&selectedPlotId!==select.value)selectedPlotId=select.value;
 if(old!==select.value){select.dispatchEvent(new Event('change',{bubbles:true}));window.GrowPlotRefreshHomeStreams?.();}
}


// Compact tree in the main Plot selector: owner → plot → greenhouse → section.
// The native plot selector remains the source of truth for existing screens.
function renderHomePlotTree(){
 const select=document.getElementById('plot');if(!select||!select.parentElement)return;
 let host=document.getElementById('gpHomePlotTree');
 if(!host){
  // Keep the original select available for existing application logic, but
  // replace its visible dropdown with an accessible tree popover.
  const wrapper=document.createElement('div');wrapper.id='gpHomePlotPicker';wrapper.style.cssText='position:relative;min-width:0;width:100%';
  const trigger=document.createElement('button');trigger.id='gpHomePlotTrigger';trigger.type='button';
  trigger.setAttribute('aria-expanded','false');trigger.setAttribute('aria-controls','gpHomePlotTree');
  trigger.style.cssText='width:100%;box-sizing:border-box;text-align:left;padding:10px 32px 10px 12px;border:1px solid #a9c7b1;border-radius:9px;background:#fff;color:#173e28;font:inherit;cursor:pointer';
  host=document.createElement('div');host.id='gpHomePlotTree';host.hidden=true;
  host.style.cssText='position:absolute;top:calc(100% + 4px);left:0;right:0;z-index:5000;max-height:320px;min-width:280px;overflow:auto;border:1px solid #cbd8ce;border-radius:9px;padding:8px;font-size:13px;color:#173e28!important;background:#fff;box-shadow:0 9px 24px #0002';
  select.insertAdjacentElement('afterend',wrapper);wrapper.append(trigger,host);
  select.style.cssText+=';position:absolute!important;opacity:0!important;pointer-events:none!important;width:1px!important;height:1px!important;overflow:hidden!important';
  select.tabIndex=-1;select.setAttribute('aria-hidden','true');
  const toggle=open=>{host.hidden=!open;trigger.setAttribute('aria-expanded',String(open));};
  trigger.onclick=()=>toggle(host.hidden);
  document.addEventListener('pointerdown',event=>{if(!wrapper.contains(event.target))toggle(false);});
  trigger.addEventListener('keydown',event=>{if(event.key==='Escape'){toggle(false);trigger.focus();}});
  host.addEventListener('keydown',event=>{if(event.key==='Escape'){toggle(false);trigger.focus();}});
  window.GrowPlotClosePlotTree=()=>toggle(false);
 }
 const trigger=document.getElementById('gpHomePlotTrigger');
 if(trigger){const chosen=Array.from(select.options).find(o=>o.value===select.value);trigger.textContent=(chosen?chosen.textContent:'Select Plot')+' ▾';}
 const profile=window.GrowPlotCloudProfile;
 const authorized=Array.isArray(profile?.plots)?profile.plots:[];
 const visible=new Set(Array.from(select.options).map(o=>o.dataset.plotKey));
 const plots=[{id:'plot-1',name:'Demo Plot',ownerEmail:'Demo'},...authorized.filter(p=>p&&visible.has(String(p.ownerUserId||p.ownerUid||String(window.GrowPlotCurrentUser?.uid||window.GrowPlotCurrentUser?.id||''))+':'+String(p.id))&&p.id!=='plot-1')];
 if(!authorized.length&&window.GrowPlotCurrentUser&&!host.dataset.profileRequested){host.dataset.profileRequested='1';if(api?.configured())request('profile',{});}
 const key=JSON.stringify(plots.map(p=>[p.ownerUserId,p.ownerEmail,p.id,p.name,p.greenhouseCount,p.sectionsPerGreenhouse,select.value]));
 if(host.dataset.treeKey===key)return;host.dataset.treeKey=key;host.replaceChildren();
 const groups=new Map();
 for(const p of plots){const owner=String(p.ownerEmail||p.ownerName||p.ownerDisplayName||((p.ownerUserId===String(window.GrowPlotCurrentUser?.uid||window.GrowPlotCurrentUser?.id||''))?'My Plots':p.ownerUserId)||'Owner');if(!groups.has(owner))groups.set(owner,[]);groups.get(owner).push(p);}
 for(const [owner,items] of groups){
  const ownerNode=document.createElement('details');ownerNode.open=true;
  const ownerLabel=document.createElement('summary');ownerLabel.textContent='👤 '+owner;ownerLabel.style.cssText='font-weight:700;padding:8px 6px;cursor:pointer;color:#173e28!important;font-size:14px!important;line-height:1.5!important;white-space:normal!important;overflow-wrap:anywhere!important;opacity:1!important;visibility:visible!important';ownerNode.appendChild(ownerLabel);
  for(const p of items){
   const plotNode=document.createElement('details');plotNode.open=select.selectedOptions[0]?.dataset.plotKey===String(p.ownerUserId||p.ownerUid||String(window.GrowPlotCurrentUser?.uid||window.GrowPlotCurrentUser?.id||''))+':'+String(p.id);plotNode.style.marginLeft='14px';
   const plotLabel=document.createElement('summary');plotLabel.style.cssText='padding:8px 6px;cursor:pointer;color:#173e28!important;font-size:14px!important;line-height:1.5!important;white-space:normal!important;overflow-wrap:anywhere!important;opacity:1!important;visibility:visible!important';plotLabel.textContent='🌱 '+String(p.name||p.plotName||p.id);plotLabel.onclick=()=>{const key=String(p.ownerUserId||p.ownerUid||String(window.GrowPlotCurrentUser?.uid||window.GrowPlotCurrentUser?.id||''))+':'+String(p.id);const option=Array.from(select.options).find(o=>o.dataset.plotKey===key);if(option){select.selectedIndex=option.index;window.GrowPlotSelectedOwnerPlotKey=key;window.GrowPlotSelectedPlotOwnerUid=p.ownerUserId||p.ownerUid;select.dispatchEvent(new Event('change',{bubbles:true}));}window.GrowPlotClosePlotTree?.();};plotNode.appendChild(plotLabel);
   const ghCount=Math.min(100,Math.max(0,Number(p.greenhouseCount)||0)),secCount=Math.min(100,Math.max(0,Number(p.sectionsPerGreenhouse)||0));
   for(let g=1;g<=ghCount;g++){
    const greenhouse=document.createElement('details');greenhouse.style.marginLeft='16px';
    const ghLabel=document.createElement('summary');ghLabel.textContent='🏡 Greenhouse '+g;ghLabel.style.cssText='padding:7px 6px;cursor:pointer;color:#173e28!important;font-size:13px!important;line-height:1.5!important;white-space:normal!important;opacity:1!important;visibility:visible!important';greenhouse.appendChild(ghLabel);
    for(let n=1;n<=secCount;n++){const section=document.createElement('div');section.textContent='└ Section '+n;section.style.cssText='margin-left:18px;padding:5px 0;color:#4c6657!important;font-size:13px!important;line-height:1.5!important;opacity:1!important;visibility:visible!important';greenhouse.appendChild(section);}
    plotNode.appendChild(greenhouse);
   }
   ownerNode.appendChild(plotNode);
  }
  host.appendChild(ownerNode);
 }
}
window.GrowPlotSyncAuthorizedHomePlots=syncAuthorizedHomePlots;
// The original Home renderer repopulates #plot; reapply owner restrictions after render.
(function installHomePlotOwnerGuard(){
 function start(){
   const root=document.getElementById('plot')?.parentElement||document.body;
   let queued=false;
   const observer=new MutationObserver(()=>{
     if(queued)return;
     queued=true;
     setTimeout(()=>{queued=false;syncAuthorizedHomePlots();},0);
   });
   observer.observe(root,{childList:true,subtree:true});
   setInterval(syncAuthorizedHomePlots,900);
   syncAuthorizedHomePlots();
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start,{once:true});
 else start();
})();
document.addEventListener('change',event=>{
 if(event.target?.id==='plot')setTimeout(()=>{
   if(typeof fillPlotSelectors==='function')fillPlotSelectors();
   syncAuthorizedHomePlots();
 },0);
});
function demo(){if(typeof plots!=='undefined'&&Array.isArray(plots)){const p=plots.find(p=>/demo/i.test(String(p.name||'')+' '+String(p.id||'')))||plots[0];if(p){selectedPlotId=p.id;if(typeof fillPlotSelectors==='function')fillPlotSelectors();if(typeof render==='function')render();}}}
window.GrowPlotShowDemoHome=function(){demo();goHome();};
function adminStats(){
 let panel=document.getElementById('growplotAdminStats');
 if(!panel){panel=document.createElement('section');panel.id='growplotAdminStats';panel.style.cssText='margin:12px;padding:12px;border:1px solid #aac;border-radius:12px;background:#f7faf7';const home=document.getElementById('mainDashboard');(home||document.body).prepend(panel);}
 panel.textContent='Loading user statistics…';request('stats',{});
}
function closeUserPage(){const page=document.getElementById('growplotUserManagementPage');if(page)page.remove();}
function userPage(){
 if(!currentUser){msg('Please log in first.');return;}
 const auth=document.getElementById('authScreen');if(auth)auth.hidden=true;
 let page=document.getElementById('growplotUserManagementPage');
 if(!page){page=document.createElement('section');page.id='growplotUserManagementPage';page.style.cssText='position:fixed;inset:0;z-index:99000;display:flex;align-items:center;justify-content:center;overflow:auto;background:#10231877;padding:16px;box-sizing:border-box';
 const back=document.createElement('button');back.type='button';back.textContent='← Back to GrowPlot';back.style.cssText='padding:12px;border-radius:8px;border:1px solid #568b66;background:white';back.onclick=()=>{closeUserPage();goHome();};
 const heading=document.createElement('h2');heading.textContent=currentUser.role==='ADMIN'?'User Statistics':'My Account';
 const details=document.createElement('div');details.id='growplotUserManagementDetails';details.style.cssText='padding:16px;margin-top:14px;border:1px solid #c4d7c8;border-radius:12px;background:white;line-height:2';
 const logout=document.createElement('button');logout.type='button';logout.textContent='Log out';logout.style.cssText='display:block;margin-top:20px;padding:12px;border:1px solid #a55;border-radius:8px;background:white';logout.onclick=()=>{window.GrowPlotBridge?.returnToDemoStreams?.();if(api&&api.logout)api.logout();window.GrowPlotDemoPowerUserUnlocked=false;window.dispatchEvent(new Event('growplot-power-user-change'));currentUser=null;window.GrowPlotCurrentUser=null;window.GrowPlotCloudProfile=null;if(typeof plots!=='undefined'&&Array.isArray(plots)){const d=plots.find(p=>p.id==='plot-1');plots.splice(0,plots.length,d||{id:'plot-1',name:'Demo Plot'});}demo();setTimeout(syncAuthorizedHomePlots,0);page.remove();if(auth)auth.hidden=false;setTimeout(()=>window.GrowPlotRefreshHomeStreams?.(),250);};
 const card=document.createElement('div');card.id='growplotUserManagementCard';card.style.cssText='width:100%;max-width:480px;max-height:calc(100vh - 32px);overflow:auto;background:#f5f8f5;padding:22px;border:1px solid #cbd8ce;border-radius:16px;box-shadow:0 16px 48px #0005;color:#163b25;box-sizing:border-box';card.append(back,heading,details,logout);page.appendChild(card);document.body.appendChild(page);
 }
 page.hidden=false;page.style.display='flex';const details=document.getElementById('growplotUserManagementDetails');
 if(currentUser.role==='ADMIN'){
   if(api&&api.configured()){details.textContent='Loading user statistics…';request('stats',{});}
   else {const n=Object.keys(localStorage).filter(k=>k.startsWith('growplot_test_account_')).length;details.textContent='Registered test users on this device: '+n+' (development only). Google Drive user statistics unavailable.';}
 }else{
   const formatDate=value=>{const date=Number.isFinite(Number(value))?new Date(Number(value)):new Date(value);return Number.isNaN(date.getTime())?'Not available':new Intl.DateTimeFormat(undefined,{dateStyle:'medium',timeStyle:'short'}).format(date);};
   details.innerHTML='';
   const rows=[['Email',currentUser.email||currentUser.username||'Not available'],['Account type','Normal User'],['Registration date',formatDate(currentUser.registrationDate)],['Last login',formatDate(currentUser.lastLoginDate)],['Payment method','Not configured']];
   for(const [label,value] of rows){const row=document.createElement('p');row.style.margin='0 0 8px';const name=document.createElement('strong');name.textContent=label+': ';row.append(name,document.createTextNode(value));details.appendChild(row);}
   installPlotVideoSharing(details);
   const allow=document.createElement('button');allow.type='button';allow.textContent='Allow Access';allow.style.cssText='padding:10px 14px;margin-top:10px;border:1px solid #72987b;border-radius:8px;background:white';allow.onclick=showAllowAccess;details.appendChild(allow);
 }
}
// Owner-granted plot video access: the account view never exposes camera passwords.
function installPlotVideoSharing(details){
 const uid=String(currentUser?.uid||currentUser?.id||'');
 const section=document.createElement('section');section.id='gpPlotVideoSharing';
 section.style.cssText='margin-top:12px;padding-top:12px;border-top:1px solid #cbd8ce;line-height:1.4;min-width:0';
 const tabs=document.createElement('div');tabs.style.cssText='display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-bottom:12px';
 const grantedTab=document.createElement('button');grantedTab.type='button';grantedTab.textContent='Granted Plot + User';
 const receivedTab=document.createElement('button');receivedTab.type='button';receivedTab.textContent='Plot Granted to Me';
 const grantedPane=document.createElement('div'),receivedPane=document.createElement('div');
 const switchTab=(name)=>{
  const first=name==='granted';grantedPane.hidden=!first;receivedPane.hidden=first;
  for(const [button,active] of [[grantedTab,first],[receivedTab,!first]]){
   button.setAttribute('aria-pressed',String(active));
   button.style.cssText='min-width:0;padding:10px 5px;border:1px solid #72987b;border-radius:9px;font-size:12px;font-weight:650;white-space:normal;overflow-wrap:anywhere;background:'+(active?'#24643c':'#fff')+';color:'+(active?'#fff':'#24643c');
  }
 };
 grantedTab.onclick=()=>switchTab('granted');receivedTab.onclick=()=>switchTab('received');
 tabs.append(grantedTab,receivedTab);
 const label=document.createElement('label');label.textContent='Select my plot';
 const plotSelect=document.createElement('select');plotSelect.id='gpSharePlot';plotSelect.style.cssText='display:block;width:100%;box-sizing:border-box;padding:9px;margin:7px 0';
 const owned=(Array.isArray(window.GrowPlotCloudProfile?.plots)?window.GrowPlotCloudProfile.plots:[]).filter(p=>p&&p.id!=='plot-1'&&String(p.ownerUserId||'')===uid);
 for(const p of owned){const opt=document.createElement('option');opt.value=String(p.id);opt.textContent=String(p.name||p.id);plotSelect.appendChild(opt);}
 const email=document.createElement('input');email.id='gpShareEmail';email.type='email';email.placeholder='Registered viewer email';email.style.cssText='width:100%;box-sizing:border-box;padding:9px;margin:7px 0';
 const grant=document.createElement('button');grant.type='button';grant.id='gpShareGrantButton';grant.textContent='Grant Video View';grant.disabled=!owned.length;grant.style.cssText='padding:10px;border:1px solid #72987b;border-radius:8px;background:white';
 const status=document.createElement('p');status.id='gpShareStatus';status.setAttribute('role','status');status.textContent=owned.length?'Grant view-only access to a registered user.':'No owned plots available to share.';
 grant.onclick=()=>{if(!email.validity.valid||!email.value.trim()){status.textContent='Enter a valid registered email.';return;}grant.disabled=true;status.textContent='Saving permission…';request('grantPlotVideo',{plotId:plotSelect.value,email:email.value.trim()});};
 const outgoing=document.createElement('div');outgoing.id='gpOutgoingVideoGrants';
 const incoming=document.createElement('div');incoming.id='gpIncomingVideoGrants';
 const ownedHeading=document.createElement('h4');ownedHeading.textContent='Granted plots and users';
 const sharedHeading=document.createElement('h4');sharedHeading.textContent='Plots I can view';
 grantedPane.append(label,plotSelect,email,grant,status,ownedHeading,outgoing);
 receivedPane.append(sharedHeading,incoming);
 section.append(tabs,grantedPane,receivedPane);details.appendChild(section);switchTab('granted');
 if(api?.configured()&&uid)request('listPlotVideoAccess',{});
}
function renderPlotVideoAccess(data){
 const outgoing=document.getElementById('gpOutgoingVideoGrants'),incoming=document.getElementById('gpIncomingVideoGrants');if(!outgoing||!incoming)return;
 outgoing.replaceChildren();incoming.replaceChildren();
 const grants=Array.isArray(data.grants)?data.grants:[],shared=Array.isArray(data.shared)?data.shared:[];
 const active=grants.filter(g=>g.active);
 if(!active.length)outgoing.textContent='No active plot grants.';
 if(active.length){
  const heading=document.createElement('p');heading.textContent='Tick users to remove access, then confirm.';heading.style.cssText='font-size:12px;color:#476353;margin-bottom:7px';outgoing.appendChild(heading);
  const selection=document.createElement('div');selection.id='gpGrantRevokeSelection';
  for(const g of active){
   const row=document.createElement('label');row.style.cssText='display:flex;align-items:center;gap:9px;padding:10px 4px;border-bottom:1px solid #e0e8e1;overflow-wrap:anywhere';
   const check=document.createElement('input');check.type='checkbox';check.dataset.plotId=String(g.plotId);check.dataset.viewerUid=String(g.viewerUid);check.setAttribute('aria-label','Remove access for '+String(g.viewerEmail||'viewer')+' to '+String(g.plotId));
   const txt=document.createElement('span');txt.textContent=String(g.plotId)+' — '+String(g.viewerEmail||'Viewer');txt.style.minWidth='0';
   row.append(check,txt);selection.appendChild(row);
  }
  const remove=document.createElement('button');remove.type='button';remove.textContent='Remove Selected Access';remove.style.cssText='margin-top:10px;padding:10px;border:1px solid #aa5555;border-radius:8px;background:#fff;color:#843333';
  remove.onclick=()=>{
   const checked=Array.from(selection.querySelectorAll('input[type="checkbox"]:checked'));
   if(!checked.length){const status=document.getElementById('gpShareStatus');if(status)status.textContent='Tick at least one grant to remove.';return;}
   if(!window.confirm('Revoke video access for '+checked.length+' selected grant(s)?'))return;
   remove.disabled=true;for(const cb of checked){cb.disabled=true;request('revokePlotVideo',{plotId:cb.dataset.plotId,viewerUid:cb.dataset.viewerUid});}
  };
  outgoing.append(selection,remove);
 }
 if(!shared.length)incoming.textContent='No plots have been granted to you.';
 for(const p of shared){
  const row=document.createElement('div');row.style.cssText='padding:10px 0;border-bottom:1px solid #ddd;overflow-wrap:anywhere';
  const title=document.createElement('span');title.textContent=String(p.plotName||p.plotId)+' — '+String(p.ownerEmail||'Plot owner');row.appendChild(title);
  const open=document.createElement('button');open.type='button';open.textContent='Open Plot';open.style.marginLeft='8px';
  open.onclick=()=>{const select=document.getElementById('plot');const opt=Array.from(select?.options||[]).find(o=>o.value===p.plotId);if(!opt){const status=document.getElementById('gpShareStatus');if(status)status.textContent='Shared plot is not in the Home plot selector yet. Refresh your profile.';return;}select.value=opt.value;select.dispatchEvent(new Event('change',{bubbles:true}));closeUserPage();goHome();};
  row.appendChild(open);incoming.appendChild(row);
 }
}
function renderAccessUsers(data){
 const panel=document.getElementById('gpAllowAccessPanel');if(!panel)return;
 const status=document.getElementById('gpAllowAccessStatus');
 const list=document.getElementById('gpAllowAccessList');list.replaceChildren();
 const users=Array.isArray(data.users)?data.users:[];
 const allowed=new Set(Array.isArray(data.allowedMonitorUids)?data.allowedMonitorUids:[]);
 for(const u of users){
  const label=document.createElement('label');label.style.cssText='display:flex;gap:9px;align-items:center;padding:8px 2px';
  const cb=document.createElement('input');cb.type='checkbox';cb.value=String(u.uid);cb.checked=allowed.has(String(u.uid));
  label.append(cb,document.createTextNode(String(u.email)));list.appendChild(label);
 }
 status.textContent=users.length?'Select users allowed to monitor your farm, then save.':'No other registered users found.';
 panel.querySelector('button').disabled=false;
}
function showAllowAccess(){
 const details=document.getElementById('growplotUserManagementDetails');if(!details)return;
 let panel=document.getElementById('gpAllowAccessPanel');
 if(panel){panel.hidden=!panel.hidden;return;}
 panel=document.createElement('section');panel.id='gpAllowAccessPanel';
 panel.style.cssText='margin-top:12px;padding:12px;border:1px solid #b9d5c2;border-radius:10px';
 const title=document.createElement('strong');title.textContent='Allow Access';
 const status=document.createElement('p');status.id='gpAllowAccessStatus';status.textContent='Loading registered users from Firestore…';
 const list=document.createElement('div');list.id='gpAllowAccessList';list.style.cssText='max-height:190px;overflow:auto';
 const save=document.createElement('button');save.type='button';save.textContent='💾 Save';save.disabled=true;
 save.style.cssText='margin-top:12px;padding:10px 14px;border-radius:8px;border:1px solid #72987b;background:white';
 save.onclick=()=>{
  save.disabled=true;status.textContent='Saving to Firestore…';
  const uids=Array.from(list.querySelectorAll('input:checked')).map(el=>el.value);
  request('saveAccessUsers',{uids});
 };
 panel.append(title,status,list,save);details.appendChild(panel);
 request('listAccessUsers',{});
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
window.GrowPlotReloadOwnedDevices=function(){if(window.GrowPlotCurrentUser&&!window.GrowPlotCurrentUser.developmentOnly&&api?.configured())request('loadOwnedDevices',{});};
window.GrowPlotAccountResponse=function(response){
 if(response&&['listPlotVideoAccess','grantPlotVideo','revokePlotVideo'].includes(response.action)){
  const status=document.getElementById('gpShareStatus'),btn=document.getElementById('gpShareGrantButton');
  if(btn)btn.disabled=!document.getElementById('gpSharePlot')?.options.length;
  if(!response.ok){if(status)status.textContent='Video sharing error: '+(response.error||'Request failed');return;}
  if(response.action==='listPlotVideoAccess')renderPlotVideoAccess(response.data||{});
  else{if(status)status.textContent=response.action==='grantPlotVideo'?'Video viewing granted.':'Viewing permission revoked.';request('listPlotVideoAccess',{});}
  return;
 }

 if(response&&response.action==='listAccessUsers'){
  const status=document.getElementById('gpAllowAccessStatus');
  if(response.ok)renderAccessUsers(response.data||{});
  else if(status)status.textContent='Unable to load user list: '+(response.error||'Firestore permission denied');
  return;
 }
 if(response&&response.action==='saveAccessUsers'){
  const status=document.getElementById('gpAllowAccessStatus');
  if(status)status.textContent=response.ok?'Monitoring access list saved.':('Save failed: '+(response.error||'Firestore permission denied'));
  const save=document.querySelector('#gpAllowAccessPanel button');if(save)save.disabled=false;
  return;
 }

 if(response&&response.action==='saveVigiCloudCredentials'){const n=document.getElementById('growplotFeedCloudStatus');if(n)n.textContent=response.ok?'VIGI password encrypted and saved in Firebase.':'VIGI cloud credential save failed: '+(response.error||'Unknown error');return;}
 if(response&&response.action==='loadVigiCloudCredentials'){if(response.ok){window.vigiCctv03HasPassword=true;const n=document.getElementById('growplotFeedCloudStatus');if(n)n.textContent='VIGI credentials restored from encrypted Firebase storage.';}return;}

 if(response&&response.action==='saveDataFeeds'){const node=document.getElementById('growplotFeedCloudStatus');if(node)node.textContent=response.ok?'Firebase: CCTV01–CCTV03 saved successfully.':'Firebase save failed: '+(response.error||'Unknown error');return;}

 if(response.action==='loadOwnedDevices'){
  const status=document.getElementById('gpDeviceStatus'),list=document.getElementById('gpSavedDevices');
  if(!response.ok){if(status)status.textContent='Firebase retrieval error: '+(response.error||'Unable to load saved devices');return;}
  window.GrowPlotOwnedDeviceData=response.data||{};
  if(typeof window.GrowPlotRenderOwnedDevices==='function')window.GrowPlotRenderOwnedDevices();
  return;
 }
 if(response.action==='passwordReset'){
  if(passwordResetButton){passwordResetButton.disabled=false;passwordResetButton.textContent='Forgot password?';}
  const target=resetFeedbackTarget||status;resetFeedbackTarget=null;
  if(registrationResetButton){registrationResetButton.disabled=false;registrationResetButton.textContent='Forgot password';}
  if(target)target.textContent=response.ok?'If a GrowPlot account matches that address, a reset email will arrive shortly. Check your inbox and spam folder.':(response.error||'Could not send a reset email. Please check your connection and try again.');
  return;
 }
 if(!response.ok){if(response.action==='googleSignIn'){const createView=document.getElementById('createView');const target=createView&&!createView.hidden?document.getElementById('createStatus'):status;if(target)target.textContent=response.error||'Google sign-in failed.';}else if(response.action==='register'&&response.data&&response.data.duplicateEmail){showRegistrationReset();}else if(response.action==='register'){const target=registrationStatus();if(target)target.textContent=response.error||'Account request failed.';}else if(response.action==='profile'&&currentUser){window.GrowPlotCloudProfile=null;saveNotice('Signed in, but your Firebase cloud profile could not be loaded: '+(response.error||'Please retry.'),false);goHome();return;}else msg((response.action==='profile'?'Firebase profile could not be loaded: ':'')+(response.error||'Account request failed'));if(response.action==='saveProfile'){const callback=saveProfileCallback;saveProfileCallback=null;if(callback)callback(false,response.error||'Firebase save failed.');}if(response.action==='profile'){const authScreen=document.getElementById('authScreen');if(authScreen)authScreen.hidden=false;currentUser=null;window.GrowPlotCurrentUser=null;}if(response.action==='saveProfile')saveNotice('Firebase save failed: '+(response.error||'Please retry.'),false);if(response.action==='saveOwnedDevice'||response.action==='assignOwnedDevice'){const el=document.getElementById('gpDeviceStatus');if(el)el.textContent='Firebase error: '+(response.error||response.message||'Request failed (no error details returned).');}return;}
 const data=response.data||{};
 if(response.action==='accountWarning'){saveNotice(data.warning||'Cloud profile update failed. Your sign-in is still active.',false);return;}
 if(response.action==='login'||response.action==='register'||response.action==='googleSignIn'){
   const previousUid=currentUser&&(currentUser.uid||currentUser.id);const incomingUid=data.user&&(data.user.uid||data.user.id);
   if(previousUid&&previousUid!==incomingUid&&typeof plots!=='undefined'&&Array.isArray(plots)){for(let i=plots.length-1;i>=0;i--)if(plots[i]?.id!=='plot-1')plots.splice(i,1);}
   currentUser=data.user;window.GrowPlotCurrentUser=currentUser;
   if(currentUser&&api&&api.configured()){request('loadVigiCloudCredentials',{});window.GrowPlotReloadOwnedDevices();setTimeout(()=>window.GrowPlotRefreshHomeStreams?.(),500);}
   if(!currentUser){msg('Invalid account response');return;}
   if(currentUser.role==='ADMIN'){request('profile',{});adminStats();}
   else {request('profile',{});}
 }else if(response.action==='profile'){
   window.GrowPlotCloudProfile=data;
   // The HTML renderer reads this shared array, so remove every non-authorized
   // plot before it renders. Do not erase persisted local records.
   if(typeof plots!=='undefined'&&Array.isArray(plots)){
     const uid=String(currentUser?.uid||currentUser?.id||'');
     const authorized=Array.isArray(data.plots)?data.plots.filter(p=>p&&p.id&&
       true):[];
     const demoPlot=plots.find(p=>p&&p.id==='plot-1')||{id:'plot-1',name:'Demo Plot'};
     plots.splice(0,plots.length,demoPlot,...authorized);
     selectedPlotId=authorized.some(p=>p.id===data.selectedPlotId)?data.selectedPlotId:'plot-1';
   }
   window.GrowPlotReloadOwnedDevices();
   setTimeout(syncAuthorizedHomePlots,0);
   // Never replace the Demo Plot with a stale device-local user record.
   if(Array.isArray(data.plots)&&data.plots.length){
     if(typeof plots!=='undefined'&&Array.isArray(plots)){
       // The shared plot model has already been rebuilt from authorized Firebase records.
       selectedPlotId=data.plots.some(p=>p.id===data.selectedPlotId)?data.selectedPlotId:'plot-1';
       if(typeof fillPlotSelectors==='function')fillPlotSelectors();
     }
     goHome();setTimeout(()=>{syncAuthorizedHomePlots();window.GrowPlotRefreshHomeStreams?.();},650);
   }else{if(typeof fillPlotSelectors==='function')fillPlotSelectors();goHome();setTimeout(syncAuthorizedHomePlots,0);if(typeof window.GrowPlotNewUserPreview==='function')window.GrowPlotNewUserPreview();}
 }else if(response.action==='saveOwnedDevice'||response.action==='assignOwnedDevice'){
   const node=document.getElementById('gpDeviceStatus');
   if(node)node.textContent=response.ok?(response.action==='saveOwnedDevice'?'✓ Device saved successfully to Firebase.':'✓ Device assigned successfully.'):('Firebase error: '+(response.error||'Request failed (no error details returned)'));if(response.ok&&response.action==='saveOwnedDevice'){const password=document.getElementById('gpDevicePassword');if(password)password.value='';window.GrowPlotReloadOwnedDevices();}if(response.ok&&response.action==='assignOwnedDevice'){window.GrowPlotReloadOwnedDevices();window.GrowPlotRefreshHomeStreams?.();}
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
window.GrowPlotAdminVerified=function(){window.GrowPlotDemoPowerUserUnlocked=true;window.dispatchEvent(new Event('growplot-power-user-change'));currentUser={username:'growplot_admin',role:'ADMIN',developmentOnly:true};window.GrowPlotCurrentUser=currentUser;goHome();demo();const home=document.getElementById('mainDashboard')||document.body;let panel=document.getElementById('growplotAdminStats');if(!panel){panel=document.createElement('section');panel.id='growplotAdminStats';home.prepend(panel);}const n=Object.keys(localStorage).filter(k=>k.startsWith('growplot_test_account_')).length;panel.textContent='Development user stats: '+n+' accounts on this device. Google Drive stats pending.';};
window.GrowPlotSaveCloudProfile=function(callback){
 if(!currentUser){saveNotice('Sign in before saving your user profile.',false);if(callback)callback(false,'Sign in first.');return;}if(currentUser.role==='ADMIN'){if(callback)callback(true,'');return;}
 if(!api||!api.configured()){saveNotice('Firebase unavailable. Data was not saved.',false);if(callback)callback(false,'Firebase is unavailable.');return;}
 const uid=String(currentUser.uid||currentUser.id||'');
 const mine=typeof plots==='undefined'?[]:plots.filter(p=>p&&p.id&&p.id!=='plot-1'&&!/demo/i.test(String(p.name||''))&&String(p.ownerUserId||'')===uid);
 saveProfileCallback=callback||null;saveNotice('Saving to Firebase…',false);request('saveProfile',{plots:mine,settings:{},selectedPlotId:typeof selectedPlotId==='undefined'?null:selectedPlotId});
};

// Plot structure controls: persisted as part of each plot's Firebase profile.
(function installPlotStructureFields(){
 function mount(){
  const name=document.getElementById('plotName');
  if(!name||document.getElementById('plotGreenhouseCount'))return;
  const wrap=document.createElement('div');wrap.className='gp-plot-structure';
  wrap.innerHTML='<style>.gp-plot-structure{display:flex;flex-wrap:wrap;gap:12px;margin:12px 0}.gp-plot-structure label{display:flex;flex-direction:column;gap:5px;flex:1 1 150px;font-size:13px;font-weight:600}.gp-plot-structure input{width:100%;max-width:200px;box-sizing:border-box;padding:9px;border:1px solid #cbd5d1;border-radius:8px;background:#fff;color:#172e26}</style><label>Number of Greenhouses<input id="plotGreenhouseCount" type="number" min="1" max="100" step="1" value="1"></label><label>Number of Sections per Greenhouse<input id="plotSectionCount" type="number" min="1" max="100" step="1" value="1"></label>';
  name.closest('label')?.insertAdjacentElement('afterend',wrap);
  const select=document.getElementById('plotManagerSelect');
  const sync=()=>{const plot=typeof plots!=='undefined'&&Array.isArray(plots)?plots.find(p=>p.id===select?.value):null;document.getElementById('plotGreenhouseCount').value=plot?.greenhouseCount||1;document.getElementById('plotSectionCount').value=plot?.sectionsPerGreenhouse||1;};
  select?.addEventListener('change',sync);
  document.getElementById('savePlotBtn')?.addEventListener('click',()=>{
   const gh=Number(document.getElementById('plotGreenhouseCount').value),sections=Number(document.getElementById('plotSectionCount').value);
   if(!Number.isInteger(gh)||gh<1||gh>100||!Number.isInteger(sections)||sections<1||sections>100){document.getElementById('plotManagementStatus').textContent='Enter valid greenhouse and section counts (1–100).';return;}
   const plot=typeof plots!=='undefined'&&Array.isArray(plots)?plots.find(p=>p.id===select?.value):null;
   if(plot){plot.greenhouseCount=gh;plot.sectionsPerGreenhouse=sections;}
  },true);
  sync();
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',mount,{once:true});else mount();
})();

// Demo plot is read-only unless the development Power User login has been verified.
// This UI guard is not a substitute for server-side authorization of cloud writes.
window.GrowPlotDemoPowerUserUnlocked=false;
(function installDemoPlotGuard(){
 const protectedIds=new Set(['plot-1']);
 const manager=()=>document.getElementById('plotManagerSelect');
 const locked=()=>protectedIds.has(manager()?.value)&&window.GrowPlotDemoPowerUserUnlocked!==true;
 const fields=['plotNumber','plotName','plotAddress','plotLatitude','plotLongitude','plotDescription','plotGreenhouseCount','plotSectionCount'];
 const actions=['savePlotBtn','deletePlotBtn','pickPlotOnMapBtn','useMapPinBtn','createDefaultFeedBtn','chooseCustomFeedBtn'];
 function refresh(){
  const isLocked=locked();
  for(const id of [...fields,...actions]){
   const element=document.getElementById(id);
   if(element){element.disabled=isLocked;element.setAttribute('aria-disabled',String(isLocked));}
  }
  const status=document.getElementById('plotManagementStatus');
  if(isLocked&&status&&status.textContent!=='Demo Plot is read-only. Select Login as Power User to unlock editing.')status.textContent='Demo Plot is read-only. Select Login as Power User to unlock editing.';
 }
 document.addEventListener('change',e=>{if(e.target?.id==='plotManagerSelect')queueMicrotask(refresh);},true);
 document.addEventListener('click',e=>{
  const element=e.target?.closest?.('#savePlotBtn,#deletePlotBtn,#pickPlotOnMapBtn,#useMapPinBtn,#createDefaultFeedBtn,#chooseCustomFeedBtn');
  if(!element||!locked())return;
  e.preventDefault();e.stopImmediatePropagation();refresh();
 },true);
 // Prevent direct edits to demo fields, even when the page rerenders.
 document.addEventListener('beforeinput',e=>{
  if(locked()&&fields.includes(e.target?.id)){e.preventDefault();}
 },true);
 window.addEventListener('growplot-power-user-change',refresh);
 // Avoid a MutationObserver feedback loop: updating status.textContent mutates
 // the observed subtree and previously froze navigation throughout the app.
 const start=()=>{refresh();};
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start,{once:true});else start();
})();



// Legacy camera IP settings apply only to the demonstration plot.
// User-owned plots must use their Firebase device assignments instead.
(function labelDemoCameraSettings(){
 function mount(){
  const section=document.getElementById('cameraIpSettingsSection');
  const summary=section?.querySelector('summary');
  if(!summary)return;
  const title=summary.querySelector('span');
  if(title)title.textContent='Camera & IoT Setting (Demo Plot)';
  else summary.insertAdjacentText('afterbegin','Camera & IoT Setting (Demo Plot) ');
  section.dataset.demoOnly='true';
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',mount,{once:true});else mount();
})();

// Cloud Data Feed registry: CCTV01–CCTV03, authenticated and scoped to Firebase UID.
// Preserve the existing on-device camera settings; never upload camera passwords.
(function installDataFeedSettings(){
 function init(){
  const button=document.getElementById('saveCctvIps');
  if(!button)return;
  const cloud=document.createElement('p');cloud.id='growplotFeedCloudStatus';cloud.className='plot-status';
  cloud.setAttribute('aria-live','polite');cloud.textContent='Firebase sync: not yet saved.';
  button.insertAdjacentElement('afterend',cloud);
  button.addEventListener('click',()=>{
   // Run after the existing local-save handler; do not sync invalid addresses.
   const status=document.getElementById('cctvIpStatus');
   const fields=[1,2,3].map(i=>document.getElementById('cctvIp'+i));
   if(fields.some(f=>!f)){cloud.textContent='Camera fields unavailable.';return;}
   const ips=fields.map(f=>f.value.trim());
   if(ips.some(ip=>ip&&!/^(10\\.|192\\.168\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.)\\d{1,3}(\\.\\d{1,3}){2}$/.test(ip))){cloud.textContent='Correct the camera IP addresses before Firebase sync.';return;}
   if(!api||!api.configured()||!window.GrowPlotCurrentUser||window.GrowPlotCurrentUser.developmentOnly){
    cloud.textContent='Saved locally only. Sign in with Firebase to sync CCTV01–CCTV03.';return;
   }
   cloud.textContent='Saving CCTV01–CCTV03 to Firebase…';
   request('saveDataFeeds',{feeds:ips.map((ip,i)=>({deviceId:'CCTV0'+(i+1),deviceType:'AV',ipAddress:ip}))});
   const username=document.getElementById('vigiCctv03Username')?.value.trim()||'';
   const password=document.getElementById('vigiCctv03Password')?.value||'';
   if(username&&password)request('saveVigiCloudCredentials',{username,password});

  });
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});else init();
})();


(function installPlotDeviceAssignments(){
 function mount(){
  const anchor=document.getElementById('plotManagerSelect');
  if(!anchor||document.getElementById('growplotOwnedDevices'))return;
  const section=document.createElement('section');
  section.id='growplotOwnedDevices';
  section.style.cssText='max-width:100%;box-sizing:border-box;margin:14px 0;padding:12px;border:1px solid #a0aaa0;border-radius:10px';
  section.innerHTML="<style>#growplotOwnedDevices{width:100%;max-width:100%;min-width:0;box-sizing:border-box;overflow-wrap:anywhere}#growplotOwnedDevices .gp-device-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}#growplotOwnedDevices label{display:flex;flex-direction:column;gap:5px;min-width:0;font-size:13px;font-weight:600}#growplotOwnedDevices input,#growplotOwnedDevices select{display:block;width:100%;max-width:100%;min-width:0;box-sizing:border-box;min-height:40px;padding:8px 10px;border:1px solid #cbd5d1;border-radius:7px;font-size:14px;background:#fff;color:#172e26}#growplotOwnedDevices .gp-device-actions{display:flex;flex-wrap:wrap;align-items:center;justify-content:flex-start;gap:10px;margin-top:16px}#growplotOwnedDevices .gp-device-actions button{flex:0 1 auto;width:auto;max-width:100%;min-height:40px;white-space:normal;border:1px solid #176b48;border-radius:10px;padding:10px 16px;background:#176b48;color:#fff;font:inherit;font-weight:700;cursor:pointer;box-shadow:0 2px 5px rgba(15,73,48,.15);transition:background .15s ease,transform .15s ease}#growplotOwnedDevices .gp-device-actions button:hover{background:#115537}#growplotOwnedDevices .gp-device-actions button:active{transform:translateY(1px)}#growplotOwnedDevices .gp-device-actions button:focus-visible{outline:3px solid #a5d6b8;outline-offset:2px}#growplotOwnedDevices #gpAssignDevice{background:#fff;color:#176b48}#growplotOwnedDevices #gpAssignDevice:hover{background:#eaf5ee}#growplotOwnedDevices #gpDeviceStatus{min-height:18px;margin:12px 0 0;font-size:13px}@media(max-width:620px){#growplotOwnedDevices .gp-device-grid{grid-template-columns:minmax(0,1fr)}#growplotOwnedDevices .gp-device-actions{flex-direction:row;align-items:center}#growplotOwnedDevices .gp-device-actions button{width:auto;max-width:100%}}</style><h3>Camera &amp; IoT Settings — Selected Plot</h3><p>Assign devices only to plots and sections in your account.</p><div class=\"gp-device-grid\"><label for=\"gpDeviceId\">Device ID<input id=\"gpDeviceId\" placeholder=\"CCTV03 or IOT01\" maxlength=\"100\"></label><label for=\"gpDeviceType\">Device type<select id=\"gpDeviceType\"><option value=\"AV\">Camera</option><option value=\"IoT\">IoT</option></select></label><label for=\"gpDeviceHost\">IP address / Host<input id=\"gpDeviceHost\" placeholder=\"192.168.1.228\"></label><label for=\"gpDevicePort\">Port<input id=\"gpDevicePort\" type=\"number\" min=\"1\" max=\"65535\" value=\"554\"></label><label for=\"gpDeviceProtocol\">Protocol<select id=\"gpDeviceProtocol\"><option>RTSP</option><option>HTTP</option><option>HTTPS</option><option>MQTT</option></select></label><label for=\"gpDevicePath\">Stream / endpoint<input id=\"gpDevicePath\" placeholder=\"/stream1\"></label><label for=\"gpDeviceUser\">Device username (optional)<input id=\"gpDeviceUser\" autocomplete=\"off\"></label><label for=\"gpDevicePassword\">Device password (optional, encrypted in cloud)<input id=\"gpDevicePassword\" type=\"password\" autocomplete=\"new-password\"></label><label for=\"gpGreenhouseId\">Greenhouse ID (optional)<input id=\"gpGreenhouseId\" placeholder=\"gh-1\"></label><label for=\"gpSectionId\">Section ID<input id=\"gpSectionId\" placeholder=\"section-1\"></label></div><div class=\"gp-device-actions\"><button type=\"button\" id=\"gpSaveDevice\"><span aria-hidden=\"true\">💾</span> Save Device</button><button type=\"button\" id=\"gpAssignDevice\"><span aria-hidden=\"true\">🔗</span> Assign Device</button></div><p id=\"gpDeviceStatus\" role=\"status\" aria-live=\"polite\"></p>";
  anchor.parentElement.insertAdjacentElement('afterend',section);
  // Pairing targets are derived from the selected plot, never free-text IDs.
  const ghInput=document.getElementById('gpGreenhouseId'),secInput=document.getElementById('gpSectionId');
  function asDropdown(input){const select=document.createElement('select');select.id=input.id;select.setAttribute('aria-label',input.id==='gpGreenhouseId'?'Greenhouse':'Section');input.replaceWith(select);return select;}
  const ghSelect=asDropdown(ghInput),secSelect=asDropdown(secInput);
  const currentPlot=()=>{
   if(typeof plots==='undefined'||!Array.isArray(plots))return null;
   const uid=String(window.GrowPlotCurrentUser?.uid||window.GrowPlotCurrentUser?.id||currentUser?.uid||'');
   return plots.find(p=>p&&p.id===anchor.value&&String(p.ownerUserId||p.ownerUid||uid)===uid)||null;
  };
  const count=(v)=>Number.isInteger(Number(v))&&Number(v)>0?Math.min(Number(v),100):1;
  function populate(select,n,prefix){const old=select.value;select.replaceChildren();const placeholder=document.createElement('option');placeholder.value='';placeholder.textContent='Select '+prefix;select.appendChild(placeholder);for(let i=1;i<=n;i++){const opt=document.createElement('option');opt.value=prefix.toLowerCase()+'-'+i;opt.textContent=prefix+' '+String(i).padStart(2,'0');select.appendChild(opt);}if([...select.options].some(o=>o.value===old))select.value=old;}
  function refreshTargets(){const plot=currentPlot();populate(ghSelect,count(plot?.greenhouseCount),'Greenhouse');populate(secSelect,ghSelect.value?count(plot?.sectionsPerGreenhouse):0,'Section');}
  anchor.addEventListener('change',refreshTargets);ghSelect.addEventListener('change',()=>{populate(secSelect,ghSelect.value?count(currentPlot()?.sectionsPerGreenhouse):0,'Section');});
  document.getElementById('savePlotBtn')?.addEventListener('click',()=>setTimeout(refreshTargets,0));
  refreshTargets();
  const list=document.createElement('div');list.id='gpSavedDevices';list.style.cssText='margin-top:14px;display:grid;gap:8px';section.appendChild(list);
  function selectSavedDevice(d,a){
   document.getElementById('gpDeviceId').value=d.deviceId||'';
   for(const [id,key] of [['gpDeviceHost','ipAddress'],['gpDevicePort','port'],['gpDeviceProtocol','protocol'],['gpDevicePath','streamPath'],['gpDeviceUser','username'],['gpDeviceType','deviceType']]){
    const el=document.getElementById(id);if(el&&d[key]!=null)el.value=String(d[key]);
   }
   if(a){ghSelect.value=a.greenhouseId;ghSelect.dispatchEvent(new Event('change'));secSelect.value=a.sectionId;}
   document.getElementById('gpDeviceStatus').textContent=a?'Loaded saved assignment.':'Saved device loaded. Select greenhouse and section, then Assign Device.';
  }
  window.GrowPlotRenderOwnedDevices=function(){
   const data=window.GrowPlotOwnedDeviceData||{},devices=Array.isArray(data.devices)?data.devices:[],assignments=Array.isArray(data.assignments)?data.assignments:[];
   const plotId=anchor.value;list.replaceChildren();
   const heading=document.createElement('strong');heading.textContent='Saved Cameras & IoT — '+(anchor.selectedOptions[0]?.textContent||plotId);list.appendChild(heading);
   if(plotId==='plot-1'){const msg=document.createElement('p');msg.textContent='Demo Plot uses Camera & IoT Setting (Demo Plot).';list.appendChild(msg);return;}
   const matches=assignments.filter(a=>a.plotId===plotId);
   const info=document.createElement('p');info.textContent=matches.length+' assigned to this plot · '+devices.length+' saved in your account';list.appendChild(info);
   const shown=new Set();
   for(const a of matches){
    const d=devices.find(x=>x.deviceId===a.deviceId)||{deviceId:a.deviceId,deviceName:a.deviceId};shown.add(a.deviceId);
    const item=document.createElement('button');item.type='button';item.style.cssText='text-align:left;background:#f2f7f3;border:1px solid #a7c7b1;border-radius:8px;padding:10px;color:#193b2b';
    item.textContent='✓ '+(d.deviceName||d.deviceId)+' — '+(a.greenhouseId||'Greenhouse unspecified')+' / '+(a.sectionId||'Section unspecified')+(d.ipAddress?' — '+d.ipAddress:'');
    item.onclick=()=>selectSavedDevice(d,a);list.appendChild(item);
   }
   for(const d of devices){
    if(shown.has(d.deviceId))continue;
    const item=document.createElement('button');item.type='button';item.style.cssText='text-align:left;background:white;border:1px solid #a7c7b1;border-radius:8px;padding:10px;color:#193b2b';
    const elsewhere=assignments.filter(a=>a.deviceId===d.deviceId).map(a=>a.plotId+' / '+a.greenhouseId+' / '+a.sectionId);
    item.textContent='＋ '+(d.deviceName||d.deviceId)+' ('+(d.deviceType||'Device')+') — '+(elsewhere.length?'Assigned elsewhere: '+elsewhere.join(', '):'Not assigned to this plot')+(d.ipAddress?' — '+d.ipAddress:'');
    item.onclick=()=>selectSavedDevice(d,null);list.appendChild(item);
   }
   if(!devices.length){const empty=document.createElement('p');empty.textContent='No saved devices found for this Firebase account.';list.appendChild(empty);}
  };
  anchor.addEventListener('change',()=>{window.GrowPlotRenderOwnedDevices();window.GrowPlotReloadOwnedDevices();});
  window.GrowPlotReloadOwnedDevices();
  const val=id=>document.getElementById(id)?.value.trim()||'';
  const status=document.getElementById('gpDeviceStatus');
  function guard(){
   if(!currentUser||currentUser.developmentOnly||!api?.configured()){status.textContent='Firebase sign-in required.';return false;}
   if(!anchor.value||anchor.value==='plot-1'){status.textContent='Select your own non-demo plot.';return false;}
   if(!currentPlot()){status.textContent='This plot is not owned by your signed-in account. Select your own plot to assign a device.';return false;}
   return true;
  }
  document.getElementById('gpSaveDevice').onclick=()=>{
   if(!guard())return;
   const port=Number(val('gpDevicePort'));
   if(!Number.isInteger(port)||port<1||port>65535){status.textContent='Enter a valid port.';return;}
   const deviceId=val('gpDeviceId'),ipAddress=val('gpDeviceHost');
   if(!/^[A-Za-z0-9_-]{1,100}$/.test(deviceId)||!/^[A-Za-z0-9.-]{1,253}$/.test(ipAddress)){status.textContent='Enter a valid device ID and IP/hostname.';return;}
   request('saveOwnedDevice',{deviceId,deviceType:val('gpDeviceType'),deviceName:deviceId,ipAddress,port,protocol:val('gpDeviceProtocol'),streamPath:val('gpDevicePath'),username:val('gpDeviceUser'),password:document.getElementById('gpDevicePassword').value});
   status.textContent='Saving device to your Firebase account…';
  };
  document.getElementById('gpAssignDevice').onclick=()=>{
   if(!guard())return;
   const plotId=anchor.value,sectionId=val('gpSectionId'),deviceId=val('gpDeviceId');
   if(!currentPlot()||!ghSelect.value||!secSelect.value){status.textContent='Select a configured greenhouse and section in this plot first.';return;}
   if(!/^[A-Za-z0-9_-]{1,100}$/.test(sectionId)||!/^[A-Za-z0-9_-]{1,100}$/.test(deviceId)){status.textContent='Enter valid section and device IDs.';return;}
   request('assignOwnedDevice',{plotId,sectionId,deviceId,greenhouseId:val('gpGreenhouseId')});
   status.textContent='Assigning device to your selected plot…';
  };
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',mount,{once:true});else mount();
})();

const preview=document.getElementById('growplotPreviewButton');if(preview)preview.remove();
// Development build permits demo administrator access; no false cloud-authentication claims.
})();


// Home streaming follows the signed-in user's plot and greenhouse selection.
// The three existing live windows represent sections 1, 2 and 3 respectively.
(function syncHomeStreamingScope(){
 function start(){
  // Home selectors are #plot and #gh. Manage Farm's selector is a separate editor.
  let previous='';
  function sync(force){
   const plot=document.getElementById('plot'),gh=document.getElementById('gh');
   if(!plot||!gh)return;
   if(!window.GrowPlotCurrentUser&&plot.value!=='plot-1')syncAuthorizedHomePlots();
   const plotId=window.GrowPlotCurrentUser?(plot.value||'plot-1'):'plot-1';
   const rawGh=String(gh.value||'1');
   const greenhouse=!window.GrowPlotCurrentUser?'greenhouse-1':(/^greenhouse-[1-9][0-9]*$/.test(rawGh)?rawGh:'greenhouse-'+(Number(rawGh)||1));
   const uid=window.GrowPlotCurrentUser?.uid||window.GrowPlotCurrentUser?.userId||'';
   const scope=uid+'|'+plotId+'|'+greenhouse;
   if(!force&&scope===previous)return;
   previous=scope;
   window.GrowPlotBridge?.setActiveStreamingContext?.(plotId,greenhouse);
  }
  document.addEventListener('change',e=>{if(e.target?.id==='plot'||e.target?.id==='gh')sync(false);});
  document.addEventListener('visibilitychange',()=>{if(!document.hidden)sync(true);});
  window.GrowPlotRefreshHomeStreams=()=>sync(true);
  setInterval(()=>sync(false),1800);
  sync(true);
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start,{once:true});else start();
})();

/* Home CCTV occupancy: six fixed positions; only assigned devices are live slots.
 * Demo retains its original sample camera behaviour. */
(function installEmptyCctvSlots(){
 const css=document.createElement('style');
 css.textContent='.gp-empty-cctv .feed{background:#101513!important;position:relative!important}.gp-empty-cctv .feed::after{content:"Empty CCTV Slot";position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:#9ca9a1;font-size:clamp(14px,2vw,22px);font-weight:600;letter-spacing:.03em;background:#101513;z-index:20;pointer-events:none}.gp-empty-cctv .feedimg,.gp-empty-cctv video,.gp-empty-cctv iframe{visibility:hidden!important}.gp-empty-cctv .ipwebcam-state{visibility:hidden!important}';
 document.head.appendChild(css);
 let pending=false;
 function refresh(){
  const root=document.getElementById('cameras'),plotSelect=document.getElementById('plot'),ghSelect=document.getElementById('gh');
  if(!root||!plotSelect)return;
  const plotId=plotSelect.value,owner=String(window.GrowPlotSelectedPlotOwnerUid||window.GrowPlotCurrentUser?.uid||'');
  const gh='greenhouse-'+String(ghSelect?.value||'1').replace(/^greenhouse-/,'');
  const data=window.GrowPlotOwnedDeviceData||{};
  const assignments=Array.isArray(data.assignments)?data.assignments:[];
  const cards=Array.from(root.children).slice(0,6);
  cards.forEach((card,index)=>{
   const occupied=plotId==='plot-1'||assignments.some(a=>String(a.plotId)===plotId&&String(a.ownerUserId||owner)===owner&&String(a.greenhouseId)===gh&&String(a.sectionId)==='section-'+(index+1));
   card.classList.toggle('gp-empty-cctv',!occupied);
  });
 }
 function schedule(){if(pending)return;pending=true;requestAnimationFrame(()=>{pending=false;refresh();});}
 const observer=new MutationObserver(schedule);
 function start(){
  const root=document.getElementById('cameras');
  if(!root)return;
  observer.observe(root,{childList:true});
  document.getElementById('plot')?.addEventListener('change',schedule);
  document.getElementById('gh')?.addEventListener('change',schedule);
  schedule();
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start);else start();
 const old=window.GrowPlotRenderOwnedDevices;
 window.GrowPlotRenderOwnedDevices=function(){if(typeof old==='function')old.apply(this,arguments);schedule();};
 window.GrowPlotRefreshEmptyCctvSlots=schedule;
})();
