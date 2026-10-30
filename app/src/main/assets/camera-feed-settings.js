(function installCameraFeedSettings(){
 'use strict';
 const KEY='growplotHomeFeedMode';
 const CAMERA_MODE='device-camera',STREAM_MODE='cctv-stream';
 const readMode=()=>{
  try{return localStorage.getItem(KEY)===CAMERA_MODE?CAMERA_MODE:STREAM_MODE}catch(_){return STREAM_MODE}
 };
 const writeMode=value=>{
  const mode=value===CAMERA_MODE?CAMERA_MODE:STREAM_MODE;
  try{localStorage.setItem(KEY,mode)}catch(_){}
  return mode;
 };
 function addStyles(){
  if(document.getElementById('cameraFeedModeStyles'))return;
  const style=document.createElement('style');style.id='cameraFeedModeStyles';
  style.textContent='.feed-camera-button{display:inline-grid;place-items:center;width:30px;height:28px;padding:4px;border:1px solid #b9d4c0;border-radius:8px;background:#eef7f0;color:#174a35;cursor:pointer}.feed-camera-button svg{width:18px;height:18px}.feed-camera-button:focus-visible{outline:2px solid #438b61;outline-offset:2px}.feed-source-select[hidden]{display:none}.cctv-stream-card{margin:10px 0 0;padding:10px;border:1px solid #d9e5dc;border-radius:10px;background:#f7faf7}.cctv-stream-choice{display:flex;align-items:center;gap:6px;padding:1px 1px 8px;color:#174a35;font-size:12px;font-weight:800}.cctv-stream-choice input{accent-color:#176b48}.cctv-stream-controls{min-width:0;margin:0;padding:0;border:0}#cctvStreamControls[disabled]{opacity:.52}#cctvStreamControls :disabled{opacity:.55;cursor:not-allowed}.feed-mode-choice{display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin:0;padding:9px 10px;border:1px solid #d9e5dc;border-radius:9px;background:#f7faf7}.feed-mode-choice legend{padding:0 4px;color:#174a35;font-size:12px;font-weight:800}.feed-mode-choice label{display:flex;align-items:center;gap:6px;color:#355243;font-size:12px;font-weight:700}.feed-mode-choice input{accent-color:#176b48}.feed-mode-note{flex-basis:100%;margin:0;color:#617166;font-size:10px;line-height:1.4}';
  document.head.appendChild(style);
 }
 function applyMode(){
  const mode=readMode(),streamCard=document.getElementById('cctvStreamConfiguration'),streamControls=document.getElementById('cctvStreamControls');
  const deviceCamera=mode===CAMERA_MODE;
  if(streamCard){
   streamCard.hidden=false;
   streamCard.setAttribute('aria-disabled',String(deviceCamera));
  }
  if(streamControls){
   streamControls.setAttribute('aria-disabled',String(deviceCamera));
   streamControls.disabled=deviceCamera;
   for(const control of streamControls.querySelectorAll('input,button,select,textarea')){
    control.disabled=deviceCamera;
   }
  }
  for(const card of document.querySelectorAll('.camera')){
   const label=card.querySelector('.feed-source-label'),select=card.querySelector('.feed-source-select');
   if(!label||!select)continue;
   let button=card.querySelector('.feed-camera-button');
   if(mode===CAMERA_MODE){
    select.hidden=true;
    if(!button){
     button=document.createElement('button');button.type='button';button.className='feed-camera-button';
     button.setAttribute('aria-label','Take a photo for '+String(select.dataset.camera||'')+' image feed');
     button.title='Take a photo for this image feed';
     button.innerHTML='<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h3l1.5-2h7L17 7h3v12H4z"/><circle cx="12" cy="13" r="3.5"/></svg>';
     button.addEventListener('click',()=>{
      const bridge=window.GrowPlotBridge,camera=Number(select.dataset.camera),plot=document.getElementById('plot'),plotId=plot?.value||'';
      if(bridge&&typeof bridge.captureFeedPhoto==='function'){bridge.captureFeedPhoto(camera,plotId);return}
      if(typeof window.showSaveToast==='function')window.showSaveToast('Device camera is available in the GrowPlot Android app.');
     });
     label.insertBefore(button,select);
    }
   }else{
    select.hidden=false;
    if(button)button.remove();
   }
  }
 }
 function init(){
  addStyles();
  const mode=readMode();
  const streamControls=document.getElementById('cctvStreamControls');
  if(streamControls&&typeof MutationObserver==='function'){
   const observer=new MutationObserver(applyMode);
   observer.observe(streamControls,{childList:true,subtree:true});
  }
  for(const input of document.querySelectorAll('input[name="homeFeedMode"]')){
   input.checked=input.value===mode;
   input.addEventListener('change',()=>{
    if(!input.checked)return;
    writeMode(input.value);applyMode();
    if(typeof window.render==='function')window.render();
    applyMode();
   });
  }
  applyMode();
 }
 window.GrowPlotFeedSettings={getMode:readMode,setMode:writeMode,refresh:applyMode};
 if(typeof window.render==='function'){
  const render=window.render;
  window.render=function(){const result=render.apply(this,arguments);applyMode();return result};
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});else init();
})();
