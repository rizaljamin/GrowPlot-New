(function installCameraFeedSettings(){
 'use strict';
 const KEY='growplotHomeFeedMode',CAMERA_MODE='device-camera',STREAM_MODE='cctv-stream';
 const readMode=()=>{try{return localStorage.getItem(KEY)===CAMERA_MODE?CAMERA_MODE:STREAM_MODE}catch(_){return STREAM_MODE}};
 const writeMode=value=>{const mode=value===CAMERA_MODE?CAMERA_MODE:STREAM_MODE;try{localStorage.setItem(KEY,mode)}catch(_){}return mode};
 const escapeHtml=value=>String(value??'').replace(/[&<>"']/g,char=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
 function addStyles(){
  if(document.getElementById('cameraFeedModeStyles'))return;
  const style=document.createElement('style');style.id='cameraFeedModeStyles';
  style.textContent='.feed-card-actions{display:flex;align-items:center;gap:5px}.stream-settings-button,.feed-camera-button{display:inline-grid;place-items:center;width:32px;height:30px;padding:5px;border:1px solid #cbded0;border-radius:9px;background:#f5faf6;color:#174a35;cursor:pointer}.stream-settings-button:hover,.feed-camera-button:hover{background:#e4f2e8}.stream-settings-button svg,.feed-camera-button svg{width:18px;height:18px}.stream-settings-button:focus-visible,.feed-camera-button:focus-visible{outline:2px solid #438b61;outline-offset:2px}.stream-settings-overlay{position:fixed;inset:0;z-index:95;display:flex;align-items:center;justify-content:center;padding:14px;background:#07170dcc}.stream-settings-overlay[hidden]{display:none}.stream-settings-dialog{width:min(440px,100%);max-height:min(90dvh,760px);overflow:auto;padding:18px;border:1px solid #dce8df;border-radius:16px;background:#fff;box-shadow:0 18px 55px #0005;color:#17231c}.stream-settings-head{display:flex;align-items:center;justify-content:space-between;gap:10px;margin-bottom:6px}.stream-settings-head h3{margin:0;color:#174a35;font-size:18px}.stream-settings-close{display:grid;place-items:center;width:32px;height:32px;border:0;border-radius:50%;background:#eef4ef;color:#355243;font-size:20px}.stream-settings-help{margin:0 0 14px;color:#637269;font-size:12px;line-height:1.45}.stream-settings-form{display:grid;gap:11px}.stream-settings-form label{display:grid;gap:5px;color:#355243;font-size:11px;font-weight:800}.stream-settings-form input,.stream-settings-form select{width:100%;min-height:40px;border:1px solid #d4e0d7;border-radius:9px;background:#fff;color:#173d2c;padding:9px 10px;font:600 13px system-ui}.stream-settings-form input:focus,.stream-settings-form select:focus{outline:2px solid #80c59c;border-color:#438b61}.stream-settings-grid{display:grid;grid-template-columns:minmax(0,1fr) 120px;gap:9px}.stream-settings-credentials{display:grid;gap:10px;padding-top:3px}.stream-settings-error{min-height:16px;color:#a33224;font-size:11px}.stream-settings-actions{display:flex;justify-content:flex-end;gap:8px;padding-top:3px}.stream-settings-actions button{min-height:38px;padding:8px 13px;border:1px solid #d4e0d7;border-radius:9px;background:#f7faf7;color:#174a35;font:750 12px system-ui}.stream-settings-actions .primary{border-color:#174a35;background:#174a35;color:#fff}.stream-settings-actions button:disabled{opacity:.55}@media(max-width:420px){.stream-settings-dialog{padding:15px}.stream-settings-grid{grid-template-columns:minmax(0,1fr) 96px}}.camera-grid.single-stream{grid-template-columns:minmax(0,1fr);max-width:960px;margin-inline:auto}.camera-grid.single-stream .feed{height:clamp(260px,48vw,520px);display:grid;place-items:center}.camera-grid.single-stream .feed-camera-mode{background:#dce8dd}.stream-camera-snap{position:absolute;inset:0;z-index:2;display:grid;place-content:center;justify-items:center;gap:12px;width:100%;height:100%;border:0;background:transparent;color:#174a35;cursor:pointer;font:700 14px/1.3 system-ui}.stream-camera-snap svg{width:64px;height:64px;padding:13px;border:1px solid #b8d1bf;border-radius:50%;background:#f7fbf8;box-shadow:0 3px 12px #123f2d18}.stream-camera-snap:focus-visible{outline:3px solid #438b61;outline-offset:-6px}.stream-source-picker{position:absolute;top:calc(100% + 6px);right:8px;z-index:20;display:grid;min-width:168px;padding:5px;border:1px solid #d7e3da;border-radius:12px;background:#fff;box-shadow:0 10px 24px #102b1d2b}.camtop{position:relative}.stream-source-picker[hidden]{display:none}.stream-source-option{display:flex;align-items:center;gap:9px;width:100%;min-height:42px;padding:8px 10px;border:0;border-radius:8px;background:#fff;color:#214835;text-align:left;font:650 13px system-ui;cursor:pointer}.stream-source-option:hover,.stream-source-option[aria-selected=true]{background:#edf6ef}.stream-source-option svg{width:20px;height:20px;flex:none}';
  document.head.appendChild(style);
 }
 function fallback(camera){return {ip:camera===1?'192.168.1.241':camera===2?'192.168.1.227':camera===3?'192.168.1.228':'',protocol:camera===3?'rtsp':'http',port:camera===3?554:8080,path:camera===3?'/stream1':'/videofeed'}}
 function createDialog(camera){
  const stream=String(camera).padStart(2,'0'),overlay=document.createElement('div');
  overlay.className='stream-settings-overlay';overlay.hidden=true;overlay.setAttribute('role','presentation');
  overlay.innerHTML='<section class="stream-settings-dialog" role="dialog" aria-modal="true" aria-labelledby="streamSettingsTitle"><div class="stream-settings-head"><h3 id="streamSettingsTitle">Stream '+stream+' Connection Settings</h3><button class="stream-settings-close" type="button" aria-label="Close settings">×</button></div><p class="stream-settings-help">Set the network address used by this stream. Changes are saved for this device.</p><form class="stream-settings-form"><label>Connection protocol<select name="protocol"><option value="http">HTTP · IP camera</option><option value="https">HTTPS · IP camera</option><option value="rtsp">RTSP · CCTV</option></select></label><div class="stream-settings-grid"><label><span data-ip-label>Camera IP address</span><input name="ip" type="text" inputmode="decimal" autocomplete="off" placeholder="192.168.1.100" required></label><label><span data-port-label>Camera port</span><input name="port" type="number" inputmode="numeric" min="1" max="65535" required></label></div><label>Stream path<input name="path" type="text" autocomplete="off" placeholder="/videofeed" required></label><div class="stream-settings-credentials" hidden><label>VIGI username<input name="username" type="text" autocomplete="username"></label><label>VIGI password<input name="password" type="password" autocomplete="new-password" placeholder="Leave blank to keep saved password"></label></div><div class="stream-settings-error" role="status" aria-live="polite"></div><div class="stream-settings-actions"><button type="button" data-cancel>Cancel</button><button class="primary" type="submit">Save stream settings</button></div></form></section>';
  document.body.appendChild(overlay);
  const form=overlay.querySelector('form'),protocol=form.elements.protocol,ip=form.elements.ip,port=form.elements.port,path=form.elements.path,credentials=overlay.querySelector('.stream-settings-credentials'),error=overlay.querySelector('.stream-settings-error'),close=()=>{overlay.hidden=true};
  function updateLabels(){const rtsp=protocol.value==='rtsp';overlay.querySelector('[data-ip-label]').textContent=rtsp?'CCTV / RTSP IP address':'Camera IP address';overlay.querySelector('[data-port-label]').textContent=rtsp?'RTSP port':'Camera port';path.placeholder=rtsp?'/stream1':'/videofeed';credentials.hidden=camera!==3||!rtsp}
  protocol.addEventListener('change',updateLabels);
  overlay.querySelector('.stream-settings-close').addEventListener('click',close);overlay.querySelector('[data-cancel]').addEventListener('click',close);
  overlay.addEventListener('click',event=>{if(event.target===overlay)close()});
  document.addEventListener('keydown',event=>{if(event.key==='Escape'&&!overlay.hidden)close()});
  form.addEventListener('submit',event=>{
   event.preventDefault();error.textContent='';
   const cameraIp=ip.value.trim(),streamPort=Number(port.value),streamPath=path.value.trim(),bridge=window.GrowPlotBridge;
   if(!/^(?:(?:10|192\.168|172\.(?:1[6-9]|2\d|3[01]))\.(?:\d{1,3})\.(?:\d{1,3})|100\.(?:6[4-9]|[7-9]\d|1[01]\d|12[0-7])\.(?:\d{1,3})\.(?:\d{1,3}))$/.test(cameraIp)||cameraIp.split('.').some(part=>Number(part)>255)||cameraIp.endsWith('.0')||cameraIp.endsWith('.255')){error.textContent='Enter a private-network IPv4 address.';return}
   if(!Number.isInteger(streamPort)||streamPort<1||streamPort>65535){error.textContent='Port must be between 1 and 65535.';return}
   if(!streamPath.startsWith('/')||streamPath.includes('..')||/[?#@]/.test(streamPath)){error.textContent='Enter a stream path beginning with /.';return}
   if(!bridge||typeof bridge.saveCctvCameraStreamSettings!=='function'){error.textContent='Stream settings are available in the GrowPlot Android app.';return}
   const save=form.querySelector('[type="submit"]');save.disabled=true;
   try{
    if(!bridge.saveCctvCameraStreamSettings(camera,protocol.value,cameraIp,streamPort,streamPath)){error.textContent='GrowPlot could not save these stream settings. Check the address and try again.';return}
    if(camera===3&&protocol.value==='rtsp'&&typeof bridge.saveVigiCctv03Credentials==='function'){const username=form.elements.username.value.trim(),password=form.elements.password.value;if(username||password){if(!bridge.saveVigiCctv03Credentials(username,password)){error.textContent='The VIGI credentials could not be saved.';return}}}
    close();if(typeof window.showSaveToast==='function')window.showSaveToast('Stream '+stream+' connection settings saved.');
   }catch(_){error.textContent='Could not save stream settings. Please try again.'}
   finally{save.disabled=false}
  });
  return {overlay,form,updateLabels,close,error};
 }
 const dialogs=new Map();
 function openSettings(camera){
  let dialog=dialogs.get(camera);if(!dialog){dialog=createDialog(camera);dialogs.set(camera,dialog)}
  const bridge=window.GrowPlotBridge,settings=fallback(camera);
  try{if(bridge&&typeof bridge.getCctvCameraStreamSettings==='function'){const saved=JSON.parse(bridge.getCctvCameraStreamSettings(camera)||'{}');Object.assign(settings,saved)}
   if(camera===3&&bridge&&typeof bridge.getVigiCctv03Credentials==='function'){const saved=JSON.parse(bridge.getVigiCctv03Credentials()||'{}');dialog.form.elements.username.value=saved.username||'';dialog.form.elements.password.value='';dialog.form.elements.password.placeholder=saved.passwordConfigured?'Saved password · leave blank to keep':'Enter VIGI camera password'}
  }catch(_){}
  dialog.form.elements.protocol.value=['http','https','rtsp'].includes(settings.protocol)?settings.protocol:(camera===3?'rtsp':'http');
  dialog.form.elements.ip.value=settings.ip||'';dialog.form.elements.port.value=String(settings.port||fallback(camera).port);dialog.form.elements.path.value=settings.path||fallback(camera).path;dialog.error.textContent='';dialog.updateLabels();
  dialog.overlay.hidden=false;dialog.form.elements.ip.focus();
 }
 function streamModeKey(camera,plotId){const plot=String(plotId||document.getElementById('plot')?.value||'plot-1');return 'growplotStreamMode:'+plot+':'+Number(camera)}
 function getStreamMode(camera,plotId){try{const saved=localStorage.getItem(streamModeKey(camera,plotId));if(saved===CAMERA_MODE||saved===STREAM_MODE)return saved}catch(_){}return readMode()}
 function setStreamMode(camera,mode,plotId){const chosen=mode===CAMERA_MODE?CAMERA_MODE:STREAM_MODE;try{localStorage.setItem(streamModeKey(camera,plotId),chosen)}catch(_){}return chosen}
 function currentPlotId(){return document.getElementById('plot')?.value||'plot-1'}
 function cameraSvg(){return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h3l1.5-2h8L18 7h2v12H4z"/><circle cx="12" cy="13" r="3.5"/></svg>'}
 function cctvSvg(){return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 8.5 14.5 5l5.8 2.8-1.2 4.7-12 2.9z"/><path d="m8 15.2 1.3 5M15.7 13.3l2.2 4.5M4 8l1.4 6"/></svg>'}
 function showCameraMode(card,camera){
  const feed=card.querySelector('.feed');if(!feed)return;
  feed.classList.add('feed-camera-mode');feed.setAttribute('aria-label','Stream '+String(camera).padStart(2,'0')+' static camera snapshot');
  feed.innerHTML='<button class="stream-camera-snap" type="button" aria-label="Take a static camera snapshot for Stream '+String(camera).padStart(2,'0')+'">'+cameraSvg()+'<span>Tap to take a camera snapshot</span></button>';
  feed.querySelector('.stream-camera-snap').addEventListener('click',()=>{const bridge=window.GrowPlotBridge;if(bridge&&typeof bridge.captureFeedPhoto==='function')bridge.captureFeedPhoto(camera,currentPlotId());else if(typeof window.showSaveToast==='function')window.showSaveToast('Device camera is available in the GrowPlot Android app.')});
 }
 function showCctvMode(card,camera){
  const feed=card.querySelector('.feed');if(!feed)return;
  feed.classList.remove('feed-camera-mode');feed.removeAttribute('aria-label');feed.innerHTML='';
 }
 function applyMode(){
  const mode=readMode(),streamCard=document.getElementById('cctvStreamConfiguration'),streamControls=document.getElementById('cctvStreamControls'),deviceCamera=mode===CAMERA_MODE;
  if(streamCard){streamCard.hidden=false;streamCard.setAttribute('aria-disabled',String(deviceCamera))}
  if(streamControls){streamControls.setAttribute('aria-disabled',String(deviceCamera));streamControls.disabled=deviceCamera;for(const control of streamControls.querySelectorAll('input,button,select,textarea'))control.disabled=deviceCamera}
  for(const card of document.querySelectorAll('.camera')){
   const camera=Number(card.dataset.camera||1),selectedMode=getStreamMode(camera,currentPlotId());card.dataset.streamMode=selectedMode;
   if(selectedMode===CAMERA_MODE)showCameraMode(card,camera);else showCctvMode(card,camera);
   const oldButton=card.querySelector('.feed-camera-button');if(oldButton)oldButton.remove();
  }
  const picker=document.getElementById('homeStreamSourcePicker'),button=document.getElementById('streamSettingsToggle');
  if(picker&&button){const selected=getStreamMode(1,currentPlotId());picker.querySelectorAll('[data-source]').forEach(option=>option.setAttribute('aria-pressed',String(option.dataset.source===selected)))}
 }
 function init(){
  addStyles();const mode=readMode(),streamControls=document.getElementById('cctvStreamControls');
  if(streamControls&&typeof MutationObserver==='function'){const observer=new MutationObserver(applyMode);observer.observe(streamControls,{childList:true,subtree:true})}
  for(const input of document.querySelectorAll('input[name="homeFeedMode"]')){input.checked=input.value===mode;input.addEventListener('change',()=>{if(!input.checked)return;writeMode(input.value);applyMode();if(typeof window.render==='function')window.render();applyMode()})}
  applyMode()
 }
 window.GrowPlotFeedSettings={getMode:readMode,setMode:writeMode,getStreamMode:(camera,plotId)=>getStreamMode(Number(camera),plotId),setStreamMode:(camera,mode,plotId)=>setStreamMode(Number(camera),mode,plotId),refresh:applyMode,openStreamSettings:openSettings};
 if(typeof window.render==='function'){const render=window.render;window.render=function(){const result=render.apply(this,arguments);applyMode();return result}}
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});else init()
})();