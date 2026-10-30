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
  style.textContent='.feed-camera-button{display:inline-grid;place-items:center;width:30px;height:28px;padding:4px;border:1px solid #b9d4c0;border-radius:8px;background:#eef7f0;color:#174a35;cursor:pointer}.feed-camera-button svg{width:18px;height:18px}.feed-camera-button:focus-visible{outline:2px solid #438b61;outline-offset:2px}.feed-card-actions{display:flex;align-items:center;gap:5px}.feed-source-details{position:relative}.feed-settings-button{display:grid;place-items:center;width:30px;height:28px;padding:4px;border:1px solid #d9e5dc;border-radius:8px;background:#fff;color:#286647;cursor:pointer;list-style:none}.feed-settings-button::-webkit-details-marker{display:none}.feed-settings-button svg{width:18px;height:18px}.feed-settings-button:focus-visible{outline:2px solid #438b61;outline-offset:2px}.feed-source-panel{position:absolute;top:calc(100% + 4px);right:0;z-index:10;width:172px;padding:10px;border:1px solid #d9e5dc;border-radius:10px;background:#fff;box-shadow:0 5px 16px #123f2d24}.feed-source-panel strong{display:block;margin-bottom:8px;color:#174a35;font-size:11px}.feed-source-panel label{display:block;color:#617166;font-size:10px;font-weight:700}.feed-source-panel .feed-source-select{display:block;width:100%;max-width:none;min-height:34px;margin-top:4px;padding:6px;border-radius:7px;font:600 11px system-ui;letter-spacing:0}.feed-source-select[hidden]{display:none}.cctv-stream-card{margin:10px 0 0;padding:10px;border:1px solid #d9e5dc;border-radius:10px;background:#f7faf7}.cctv-stream-choice{display:flex;align-items:center;gap:6px;padding:1px 1px 8px;color:#174a35;font-size:12px;font-weight:800}.cctv-stream-choice input{accent-color:#176b48}.cctv-stream-controls{min-width:0;margin:0;padding:0;border:0}#cctvStreamControls[disabled]{opacity:.52}#cctvStreamControls :disabled{opacity:.55;cursor:not-allowed}.feed-mode-choice{display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin:0;padding:9px 10px;border:1px solid #d9e5dc;border-radius:9px;background:#f7faf7}.feed-mode-choice legend{padding:0 4px;color:#174a35;font-size:12px;font-weight:800}.feed-mode-choice label{display:flex;align-items:center;gap:6px;color:#355243;font-size:12px;font-weight:700}.feed-mode-choice input{accent-color:#176b48}.feed-mode-note{flex-basis:100%;margin:0;color:#617166;font-size:10px;line-height:1.4}';
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
   const label=card.querySelector('.feed-source-label');let settings=card.querySelector('.feed-source-details'),select=settings?.querySelector('.feed-source-select')||card.querySelector('.feed-source-select');
   if(!select)continue;
   let actions=card.querySelector('.feed-card-actions');
   if(!settings){
    settings=document.createElement('details');settings.className='feed-source-details';
    const summary=document.createElement('summary');summary.className='feed-settings-button';summary.title='Settings for this stream';summary.setAttribute('aria-label','Settings for Stream '+String(select.dataset.camera||''));
    summary.innerHTML='<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="m19.4 15 .1.1a1.7 1.7 0 1 1-2.4 2.4L17 17.4a1.7 1.7 0 0 0-2.9 1.2v.2a1.7 1.7 0 1 1-3.4 0v-.2a1.7 1.7 0 0 0-2.9-1.2l-.1.1a1.7 1.7 0 1 1-2.4-2.4l.1-.1a1.7 1.7 0 0 0-1.2-2.9H4a1.7 1.7 0 1 1 0-3.4h.2a1.7 1.7 0 0 0 1.2-2.9l-.1-.1a1.7 1.7 0 1 1 2.4-2.4l.1.1a1.7 1.7 0 0 0 2.9-1.2V2a1.7 1.7 0 1 1 3.4 0v.2a1.7 1.7 0 0 0 2.9 1.2l.1-.1a1.7 1.7 0 1 1 2.4 2.4l-.1.1a1.7 1.7 0 0 0 1.2 2.9h.2a1.7 1.7 0 1 1 0 3.4h-.2a1.7 1.7 0 0 0-1.2 2.9Z"/></svg>';
    const panel=document.createElement('div');panel.className='feed-source-panel';
    const heading=document.createElement('strong');heading.textContent='Stream '+String(select.dataset.camera||'')+' Settings';
    const choiceLabel=document.createElement('label');choiceLabel.textContent='Image source';choiceLabel.appendChild(select);panel.append(heading,choiceLabel);settings.append(summary,panel);
    settings.addEventListener('toggle',()=>summary.setAttribute('aria-expanded',String(settings.open)));
    select.addEventListener('change',()=>{settings.open=false});
    label.replaceWith(settings);
   }
   actions=card.querySelector('.feed-card-actions');
   if(!actions){actions=document.createElement('div');actions.className='feed-card-actions';settings.parentElement.appendChild(actions);actions.appendChild(settings)}
   let button=card.querySelector('.feed-camera-button');
   if(mode===CAMERA_MODE){
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
     actions.insertBefore(button,settings);
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
