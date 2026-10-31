/* Per-stream source and connection settings for the Home stream cards. */
(function installCameraFeedSettings(){
 'use strict';
 const GLOBAL_KEY='growplotHomeFeedMode',CAMERA_MODE='device-camera',STREAM_MODE='cctv-stream';
 const escapeHtml=value=>String(value??'').replace(/[&<>"']/g,char=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
 const localKey=camera=>'growplotStreamSourceMode_'+camera;
 const readGlobal=()=>{try{return localStorage.getItem(GLOBAL_KEY)===CAMERA_MODE?CAMERA_MODE:STREAM_MODE}catch(_){return STREAM_MODE}};
 const readMode=camera=>{const bridge=window.GrowPlotBridge;try{const saved=JSON.parse(bridge?.getCctvCameraStreamSettings?.(camera)||'{}');if(saved.sourceMode===CAMERA_MODE||saved.sourceMode===STREAM_MODE)return saved.sourceMode}catch(_){}try{const saved=localStorage.getItem(localKey(camera));if(saved===CAMERA_MODE||saved===STREAM_MODE)return saved}catch(_){}return readGlobal()};
 const writeMode=(camera,value)=>{const mode=value===CAMERA_MODE?CAMERA_MODE:STREAM_MODE;try{localStorage.setItem(localKey(camera),mode)}catch(_){}return mode};
 const fallback=camera=>({ip:camera===1?'192.168.1.241':camera===2?'192.168.1.227':camera===3?'192.168.1.228':'',protocol:camera===3?'rtsp':'http',port:camera===3?554:8080,path:camera===3?'/stream1':'/videofeed'});
 function addStyles(){
  if(document.getElementById('cameraFeedModeStyles'))return;
  const style=document.createElement('style');style.id='cameraFeedModeStyles';
  style.textContent='.camera[data-source-mode]{cursor:pointer}.feed-card-actions{display:flex;align-items:center;gap:5px}.stream-settings-button,.feed-camera-button{display:inline-grid;place-items:center;width:34px;height:32px;padding:5px;border:1px solid #cbded0;border-radius:9px;background:#f5faf6;color:#174a35;cursor:pointer}.stream-settings-button:hover,.feed-camera-button:hover{background:#e4f2e8}.stream-settings-button svg,.feed-camera-button svg{width:18px;height:18px}.stream-settings-button:focus-visible,.feed-camera-button:focus-visible{outline:2px solid #438b61;outline-offset:2px}.stream-settings-overlay{position:fixed;inset:0;z-index:95;display:flex;align-items:center;justify-content:center;padding:14px;background:#07170dcc}.stream-settings-overlay[hidden]{display:none}.stream-settings-dialog{width:min(440px,100%);max-height:min(90dvh,760px);overflow:auto;padding:18px;border:1px solid #dce8df;border-radius:16px;background:#fff;box-shadow:0 18px 55px #0005;color:#17231c}.stream-settings-head{display:flex;align-items:center;justify-content:space-between;gap:10px;margin-bottom:6px}.stream-settings-head h3{margin:0;color:#174a35;font-size:18px}.stream-settings-close{display:grid;place-items:center;width:32px;height:32px;border:0;border-radius:50%;background:#eef4ef;color:#355243;font-size:20px}.stream-settings-help{margin:0 0 14px;color:#637269;font-size:12px;line-height:1.45}.stream-settings-form{display:grid;gap:11px}.stream-settings-form label{display:grid;gap:5px;color:#355243;font-size:11px;font-weight:800}.stream-source-choice{display:grid;gap:6px;margin:0;padding:10px;border:1px solid #d4e0d7;border-radius:10px}.stream-source-choice legend{padding:0 5px;color:#355243;font-size:11px;font-weight:800}.stream-source-choice label{display:flex;align-items:center;gap:7px;font-size:13px}.stream-source-choice input{width:auto;min-height:0;padding:0;accent-color:#176b48}.stream-network-fields{display:grid;gap:10px}.stream-settings-form input,.stream-settings-form select{width:100%;min-height:40px;border:1px solid #d4e0d7;border-radius:9px;background:#fff;color:#173d2c;padding:9px 10px;font:600 13px system-ui}.stream-settings-form input:focus,.stream-settings-form select:focus{outline:2px solid #80c59c;border-color:#438b61}.stream-settings-grid{display:grid;grid-template-columns:minmax(0,1fr) 120px;gap:9px}.stream-settings-credentials{display:grid;gap:10px;padding-top:3px}.stream-settings-error{min-height:16px;color:#a33224;font-size:11px}.stream-settings-actions{display:flex;justify-content:flex-end;gap:8px;padding-top:3px}.stream-settings-actions button{min-height:38px;padding:8px 13px;border:1px solid #d4e0d7;border-radius:9px;background:#f7faf7;color:#174a35;font:750 12px system-ui}.stream-settings-actions .primary{border-color:#174a35;background:#174a35;color:#fff}.stream-settings-actions button:disabled{opacity:.55}.stream-settings-form [disabled]{opacity:.55}@media(max-width:420px){.stream-settings-dialog{padding:15px}.stream-settings-grid{grid-template-columns:minmax(0,1fr) 96px}}';
  document.head.appendChild(style);
 }
 function createDialog(camera){
  const stream=String(camera).padStart(2,'0'),overlay=document.createElement('div');
  overlay.className='stream-settings-overlay';overlay.hidden=true;overlay.setAttribute('role','presentation');
  overlay.innerHTML='<section class="stream-settings-dialog" role="dialog" aria-modal="true" aria-labelledby="streamSettingsTitle"><div class="stream-settings-head"><h3 id="streamSettingsTitle">Stream '+stream+' Settings</h3><button class="stream-settings-close" type="button" aria-label="Close settings">×</button></div><p class="stream-settings-help">Choose the image source for this stream. CCTV connection details are saved on this device.</p><form class="stream-settings-form"><fieldset class="stream-source-choice"><legend>Image source</legend><label><input type="radio" name="sourceMode" value="device-camera"> Device Camera</label><label><input type="radio" name="sourceMode" value="cctv-stream"> CCTV stream</label></fieldset><div class="stream-network-fields"><label>Connection protocol<select name="protocol"><option value="http">HTTP</option><option value="https">HTTPS</option><option value="rtsp">RTSP</option></select></label><div class="stream-settings-grid"><label>CCTV / RTSP IP address<input name="ip" type="text" inputmode="decimal" autocomplete="off" placeholder="192.168.1.100" required></label><label>RTSP port<input name="port" type="number" inputmode="numeric" min="1" max="65535" required></label></div><label>Stream path<input name="path" type="text" autocomplete="off" placeholder="/stream1" required></label><div class="stream-settings-credentials"><label>Username<input name="username" type="text" autocomplete="username"></label><label>Password<input name="password" type="password" autocomplete="new-password" placeholder="Leave blank to keep saved password"></label></div></div><div class="stream-settings-error" role="status" aria-live="polite"></div><div class="stream-settings-actions"><button type="button" data-cancel>Cancel</button><button class="primary" type="submit">Save settings</button></div></form></section>';
  document.body.appendChild(overlay);
  const form=overlay.querySelector('form'),fields=overlay.querySelector('.stream-network-fields'),error=overlay.querySelector('.stream-settings-error'),close=()=>{overlay.hidden=true};
  function updateFields(){const mode=form.elements.sourceMode.value;const enabled=mode===STREAM_MODE;fields.hidden=!enabled;for(const control of fields.querySelectorAll('input,select'))control.disabled=!enabled}
  form.querySelectorAll('input[name="sourceMode"]').forEach(input=>input.addEventListener('change',updateFields));
  form.elements.protocol.addEventListener('change',()=>{const rtsp=form.elements.protocol.value==='rtsp';form.elements.path.placeholder=rtsp?'/stream1':'/videofeed'});
  overlay.querySelector('.stream-settings-close').addEventListener('click',close);overlay.querySelector('[data-cancel]').addEventListener('click',close);
  overlay.addEventListener('click',event=>{if(event.target===overlay)close()});
  document.addEventListener('keydown',event=>{if(event.key==='Escape'&&!overlay.hidden)close()});
  form.addEventListener('submit',event=>{
   event.preventDefault();error.textContent='';
   const mode=form.elements.sourceMode.value,bridge=window.GrowPlotBridge;
   if(!bridge||typeof bridge.setCctvCameraSourceMode!=='function'){error.textContent='Per-stream settings are available in the GrowPlot Android app.';return}
   const save=form.querySelector('[type="submit"]');save.disabled=true;
   try{
    if(mode===STREAM_MODE){
     const cameraIp=form.elements.ip.value.trim(),streamPort=Number(form.elements.port.value),streamPath=form.elements.path.value.trim(),protocol=form.elements.protocol;
     if(!/^(?:(?:10|192\.168|172\.(?:1[6-9]|2\d|3[01]))\.(?:\d{1,3})\.(?:\d{1,3})|100\.(?:6[4-9]|[7-9]\d|1[01]\d|12[0-7])\.(?:\d{1,3})\.(?:\d{1,3}))$/.test(cameraIp)||cameraIp.split('.').some(part=>Number(part)>255)||cameraIp.endsWith('.0')||cameraIp.endsWith('.255')){error.textContent='Enter a private-network IPv4 address.';return}
     if(!Number.isInteger(streamPort)||streamPort<1||streamPort>65535){error.textContent='Port must be between 1 and 65535.';return}
     if(!streamPath.startsWith('/')||streamPath.includes('..')||/[?#@]/.test(streamPath)){error.textContent='Enter a stream path beginning with /.';return}
     if(typeof bridge.saveCctvCameraStreamSettings!=='function'||!bridge.saveCctvCameraStreamSettings(camera,protocol.value,cameraIp,streamPort,streamPath)){error.textContent='Could not save the CCTV address. Check the IP and try again.';return}
     const user=form.elements.username.value.trim(),password=form.elements.password.value;
     if((user||password)&&!(camera===3?bridge.saveVigiCctv03Credentials&&bridge.saveVigiCctv03Credentials(user,password):bridge.saveCctvCameraCredentials&&bridge.saveCctvCameraCredentials(camera,user,password))){error.textContent='Could not save the camera login.';return}
    }
    if(!bridge.setCctvCameraSourceMode(camera,mode)){error.textContent='Could not save this stream source.';return}
    writeMode(camera,mode);close();applyMode();if(typeof window.showSaveToast==='function')window.showSaveToast('Stream '+stream+' settings saved.');
   }catch(_){error.textContent='Could not save these stream settings. Please try again.'}
   finally{save.disabled=false}
  });
  return {overlay,form,error,updateFields};
 }
 const dialogs=new Map();
 function launchDeviceCamera(camera){
  const bridge=window.GrowPlotBridge,plot=document.getElementById('plot');
  if(bridge&&typeof bridge.captureFeedPhoto==='function')bridge.captureFeedPhoto(camera,plot?.value||'');
  else if(typeof window.showSaveToast==='function')window.showSaveToast('Device camera is available in the GrowPlot Android app.');
 }
 function openSettings(camera){
  let dialog=dialogs.get(camera);if(!dialog){dialog=createDialog(camera);dialogs.set(camera,dialog)}
  const bridge=window.GrowPlotBridge,form=dialog.form,settings=fallback(camera);let credentials={};
  try{if(bridge?.getCctvCameraStreamSettings){Object.assign(settings,JSON.parse(bridge.getCctvCameraStreamSettings(camera)||'{}'))}}catch(_){}
  try{const saved=camera===3&&bridge?.getVigiCctv03Credentials?bridge.getVigiCctv03Credentials():bridge?.getCctvCameraCredentials?.(camera);credentials=JSON.parse(saved||'{}')}catch(_){}
  const mode=readMode(camera);form.querySelectorAll('input[name="sourceMode"]').forEach(input=>input.checked=input.value===mode);
  form.elements.protocol.value=['http','https','rtsp'].includes(settings.protocol)?settings.protocol:fallback(camera).protocol;
  form.elements.ip.value=settings.ip||'';form.elements.port.value=String(settings.port||fallback(camera).port);form.elements.path.value=settings.path||fallback(camera).path;
  form.elements.username.value=credentials.username||'';form.elements.password.value='';form.elements.password.placeholder=credentials.passwordConfigured?'Saved password · leave blank to keep':'Enter camera password';dialog.error.textContent='';dialog.updateFields();
  dialog.overlay.hidden=false;dialog.form.querySelector('input[name="sourceMode"]:checked')?.focus();
 }
 function applyMode(){
  const globalDeviceCamera=readGlobal()===CAMERA_MODE;
  const globalStreamControls=document.getElementById('cctvStreamControls');
  if(globalStreamControls){globalStreamControls.disabled=globalDeviceCamera;globalStreamControls.setAttribute('aria-disabled',String(globalDeviceCamera));for(const control of globalStreamControls.querySelectorAll('input,button,select,textarea'))control.disabled=globalDeviceCamera}
  for(const card of document.querySelectorAll('.camera')){
   const settings=card.querySelector('.stream-settings-button');if(!settings)continue;
   const camera=Number(settings.dataset.camera);if(!settings.dataset.bound){settings.dataset.bound='1';settings.addEventListener('click',()=>openSettings(camera))}
   let actions=card.querySelector('.feed-card-actions');const deviceCamera=readMode(camera)===CAMERA_MODE;
   card.dataset.sourceMode=deviceCamera?CAMERA_MODE:STREAM_MODE;
   card.title=deviceCamera?'Tap this stream card to open the device camera.':'Tap this stream card to open its CCTV settings.';
   if(!card.dataset.sourceClickBound){card.dataset.sourceClickBound='1';card.addEventListener('click',event=>{if(event.target.closest('button,a,input,select,textarea,[role="button"]'))return;if(readMode(camera)===CAMERA_MODE)launchDeviceCamera(camera);else openSettings(camera)})}
   if(deviceCamera){
    let button=card.querySelector('.feed-camera-button');
    if(!button){button=document.createElement('button');button.type='button';button.className='feed-camera-button';button.setAttribute('aria-label','Take a photo for Stream '+String(camera).padStart(2,'0'));button.title='Take a photo';button.innerHTML='<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h3l1.5-2h7L17 7h3v12H4z"/><circle cx="12" cy="13" r="3.5"/></svg>';button.addEventListener('click',()=>launchDeviceCamera(camera));actions.insertBefore(button,settings)}
   }else{const button=card.querySelector('.feed-camera-button');if(button)button.remove()}
  }
 }
 function init(){
  addStyles();
  for(const input of document.querySelectorAll('input[name="homeFeedMode"]')){input.checked=input.value===readGlobal();input.addEventListener('change',()=>{if(!input.checked)return;try{localStorage.setItem(GLOBAL_KEY,input.value)}catch(_){}applyMode();if(typeof window.render==='function')window.render();applyMode()})}
  applyMode()
 }
 window.GrowPlotFeedSettings={getMode:readMode,setMode:writeMode,refresh:applyMode,openStreamSettings:openSettings};
 if(typeof window.render==='function'){const render=window.render;window.render=function(){const result=render.apply(this,arguments);applyMode();return result}}
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});else init()
})();