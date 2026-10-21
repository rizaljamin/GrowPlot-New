(function(){
'use strict';
const byId=id=>document.getElementById(id);
const intervalInput=byId('cctvSnapshotInterval');
const intervalStatus=byId('liveFeedConfigurationStatus');
try{
 const savedInterval=Number(window.GrowPlotBridge.getCctvSnapshotIntervalMinutes());
 if(Number.isFinite(savedInterval)&&savedInterval>=0.5){
  if(intervalInput)intervalInput.value=String(savedInterval);
  if(intervalStatus)intervalStatus.textContent='Current interval: '+savedInterval+' minutes.';
 }
}catch(e){}
const connect=byId('createDefaultFeedBtn');
if(connect)connect.textContent='Connect to folder';
const custom=byId('chooseCustomFeedBtn');
if(custom)custom.remove();
const help=Array.from(document.querySelectorAll('#dataFeedAccordion .settings-feed-help')).find(e=>e.textContent.includes('For the default setup'));
if(help)help.textContent='Expected folder path is based on the signed-in user and selected plot. Tap Connect to folder to create or reuse that folder in Google Drive. On first use, choose the GrowPlot parent folder once. Connection status appears separately below.';
const selected=()=>{const m=byId('plotManagerSelect'),id=(m&&m.value)||(typeof selectedPlotId!=='undefined'?selectedPlotId:''),list=typeof plots!=='undefined'?plots:[];return list.find(p=>p.id===id)||list[0]||null};
const expected=p=>{try{return window.GrowPlotBridge.getExpectedFeedPath(p.id,typeof defaultFeedFolder==='function'?defaultFeedFolder(p):p.name)||''}catch(e){return ''}};
const local=p=>{try{return window.GrowPlotBridge.getFeedLocation(p.id)||''}catch(e){return ''}};
const update=()=>{const p=selected(),label=byId('feedLocationLabel'),status=byId('folderCheckStatus');if(!p)return;const connected=local(p),path=expected(p);if(label)label.textContent=path||'Sign in and select a plot to see its Google Drive path.';if(status&&!connected&&(!status.textContent||status.textContent==='No feed scan yet.'))status.textContent='Not connected on this device. Tap Connect to folder to create or access it.'};
if(typeof window.feedLocationText==='function')window.feedLocationText=function(p){return expected(p)||'Sign in and select a plot to see its Google Drive path.'};
const originalScan=window.onSnapshotScan;
if(typeof originalScan==='function')window.onSnapshotScan=function(data){originalScan(data);update();if(data&&data.plotId===((byId('plotManagerSelect')||{}).value)&&!data.connected){const status=byId('folderCheckStatus');if(status&&data.statusText)status.textContent=data.statusText}};
['plot','plotManagerSelect'].forEach(id=>{const e=byId(id);if(e)e.addEventListener('change',update)});
['growplot-plot-profile-updated','growplot-power-user-change'].forEach(name=>window.addEventListener(name,update));
update();
})();