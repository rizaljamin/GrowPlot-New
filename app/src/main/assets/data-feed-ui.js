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
const helpTexts=Array.from(document.querySelectorAll('#dataFeedAccordion .settings-feed-help'));
helpTexts.filter(e=>e.textContent.includes('For the default setup')
 ||e.textContent.includes('Live CCTV snapshots run')
 ||e.textContent.includes('Changes are saved on this device')).forEach(e=>e.remove());
const dataFeedHeadings=Array.from(document.querySelectorAll('#dataFeedAccordion h4'));
const addInfoTip=(heading,id,title,message)=>{
 if(!heading||byId(id))return;
 if(!byId('feedConnectInfoStyle')){
 const style=document.createElement('style');
 style.id='feedConnectInfoStyle';
 style.textContent='.feed-connect-heading{display:flex;align-items:center;justify-content:space-between;gap:8px}.feed-connect-info{position:relative;display:inline-flex;flex:none}.feed-connect-info button{width:24px;height:24px;padding:0;border:1px solid #cbdace;border-radius:50%;background:#f4f8f4;color:#174a35;font:700 13px system-ui;cursor:pointer}.feed-connect-popover{position:absolute;z-index:50;right:0;top:30px;width:min(270px,calc(100vw - 92px));padding:10px 12px;border:1px solid #cbdace;border-radius:9px;background:#fff;color:#355243;box-shadow:0 7px 22px #102f2226;font:500 12px/1.45 system-ui}';
 document.head.appendChild(style);
 }
 heading.classList.add('feed-connect-heading');
 const wrap=document.createElement('span');wrap.className='feed-connect-info';
 const button=document.createElement('button');button.id=id;button.type='button';button.textContent='i';button.title=title;button.setAttribute('aria-label',title);button.setAttribute('aria-expanded','false');
 const pop=document.createElement('span');pop.id=id+'Popover';pop.className='feed-connect-popover';pop.setAttribute('role','tooltip');pop.hidden=true;
 pop.textContent=message;
 button.setAttribute('aria-controls',pop.id);wrap.append(button,pop);heading.appendChild(wrap);
 let pinned=false;
 const show=visible=>{pop.hidden=!visible;button.setAttribute('aria-expanded',String(visible))};
 wrap.addEventListener('mouseenter',()=>{if(!pinned)show(true)});
 wrap.addEventListener('mouseleave',()=>{if(!pinned)show(false)});
 button.addEventListener('click',event=>{event.stopPropagation();pinned=!pinned;show(pinned)});
 document.addEventListener('click',event=>{if(!wrap.contains(event.target)){pinned=false;show(false)}});
};
const intervalHeading=dataFeedHeadings.find(e=>e.textContent.trim()==='Photo snapshot interval');
const connectionHeading=dataFeedHeadings.find(e=>e.textContent.trim()==='Connection and scanning');
addInfoTip(intervalHeading,'feedSnapshotInfoButton','Photo snapshot interval information','Snapshots run only while GrowPlot is open and the camera stream is active. They are not scheduled background photos. Minimum interval: 0.5 minutes.');
addInfoTip(connectionHeading,'feedConnectInfoButton','Data feed connection information','Tap Connect to folder to list available paths under Projects/GrowPlot/Image_Feed, then choose any folder path. GrowPlot saves the exact folder you select.');
const selected=()=>{const m=byId('plotManagerSelect'),id=(m&&m.value)||(typeof selectedPlotId!=='undefined'?selectedPlotId:''),list=typeof plots!=='undefined'?plots:[];return list.find(p=>p.id===id)||list[0]||null};
const expected=p=>{try{return window.GrowPlotBridge.getExpectedFeedPath(p.id,typeof defaultFeedFolder==='function'?defaultFeedFolder(p):p.name)||''}catch(e){return ''}};
const local=p=>{try{return window.GrowPlotBridge.getFeedLocation(p.id)||''}catch(e){return ''}};
const driveApiConnected=p=>{try{return !!window.GrowPlotBridge.hasDriveApiFeedFolder(p.id)}catch(e){return false}};
const update=()=>{const p=selected(),label=byId('feedLocationLabel'),status=byId('folderCheckStatus');if(!p)return;const connected=local(p),apiConnected=driveApiConnected(p),path=connected||expected(p);if(label)label.textContent=path||'Sign in and select a plot to see its Google Drive path.';if(status){const text=(status.textContent||'').trim();if(apiConnected&&(!text||text==='No feed scan yet.'||/no google drive folder is connected|not connected on this device/i.test(text)))status.textContent='Google Drive connected to the assigned folder.';else if(!connected&&!apiConnected&&(!text||text==='No feed scan yet.'))status.textContent='Not connected on this device. Tap Connect to folder to retry.'}};
if(typeof window.feedLocationText==='function')window.feedLocationText=function(p){return expected(p)||'Sign in and select a plot to see its Google Drive path.'};
const originalScan=window.onSnapshotScan;
if(typeof originalScan==='function')window.onSnapshotScan=function(data){originalScan(data);update();if(data&&data.plotId===((byId('plotManagerSelect')||{}).value)&&!data.connected){const status=byId('folderCheckStatus');if(status&&data.statusText){status.textContent=data.statusText;update()}}};
window.addEventListener('growplot-drive-feed-connection-changed',update);
['plot','plotManagerSelect'].forEach(id=>{const e=byId(id);if(e)e.addEventListener('change',update)});
['growplot-plot-profile-updated','growplot-power-user-change'].forEach(name=>window.addEventListener(name,update));
update();

function cropSummaryForCamera(camera){const greenhouse=String(document.getElementById('gh')?.value||'1'),section=String(camera),records=farmPlantings.filter(record=>String(record.plotId||'')===String(selectedPlotId)&&String(record.greenhouse||'1')===greenhouse&&(String(record.section||'1')==='all'||String(record.section||'1')===section));if(!records.length)return 'No crop assigned to this section.';return records.map(record=>{const crop=farmCropCatalog.find(item=>item.id===record.cropId),name=crop?.name||'Crop';return name+' · Seedling: '+farmDateLabel(record.seedlingDate)+' · Planting: '+farmDateLabel(record.plantingDate)+' · Status: '+(record.status||'Planned')}).join('\n')}
function diagnosisChannelSummary(report,kind){if(!report)return 'Not run';const status=String(report.status||'').toLowerCase();if(status==='pending')return 'Processing';if(status==='not_configured')return 'Not configured';if(status==='error'||report.ok===false)return String(report.message||report.error||'Request failed');if(kind==='opencv'){const cv=report.openCvAnalysis||report,color=report.colorPercentages||{};const green=cv.greenPercent??color.green??color.greenPercent,yellow=cv.yellowPercent??color.yellow??color.yellowPercent,brown=cv.brownPercent??color.brown??color.brownPercent;const parts=[];if(green!=null)parts.push('green '+Number(green).toFixed(1)+'%');if(yellow!=null)parts.push('yellow '+Number(yellow).toFixed(1)+'%');if(brown!=null)parts.push('brown '+Number(brown).toFixed(1)+'%');return parts.length?parts.join(' · '):(report.metrics||report.summary||'Completed')}if(kind==='vertex'){const a=report.analysis||{},parts=[];if(a.health_assessment)parts.push(a.health_assessment);else if(a.plant_visible===false)parts.push('Plant not detected');else if(a.plant_description)parts.push(a.plant_description);if(!parts.length&&Array.isArray(a.observed_signs)&&a.observed_signs.length)parts.push(a.observed_signs.slice(0,2).join('; '));return parts.join(' · ')||report.message||'Completed'}const results=Array.isArray(report.results)?report.results:[],top=results[0],species=top?.species||{},name=species.commonNames?.[0]||species.commonName||species.scientificName||top?.name||top?.label,score=Number(top?.score??top?.confidence);if(name)return name+(Number.isFinite(score)?' · '+Math.round(score*100)+'%':'');return report.status==='ok'?'No ranked match returned':report.message||'Completed'}
function updateDashboardPlantSummary(camera){const card=cameraRoot.children[Number(camera)-1];if(!card)return;const crop=card.querySelector('[data-stat="crop-summary"]');if(crop)crop.textContent=cropSummaryForCamera(camera);const date=card.querySelector('[data-stat="diagnosis-date"]'),summary=card.querySelector('[data-stat="diagnosis-summary"]');if(!date||!summary)return;const latest=diagnosisHistoryFor(camera,selectedPlotId)[0];if(!latest){date.textContent='No diagnosis recorded.';summary.textContent='OpenCV: Not run · Vertex: Not run · Pl@ntNet: Not run';return}const result=latest.result||latest,cv=result.openCvAnalysis?{openCvAnalysis:result.openCvAnalysis}:result,vertex=result.vertexResponse||result.vertex||null,plantnet=latest.providerResult||result.plantNetResponse||result.plantnetResponse||null;date.textContent='Latest: '+diagnosisHistoryDateLabel(latest.occurredAt);summary.textContent='OpenCV: '+diagnosisChannelSummary(cv,'opencv')+'\nVertex: '+diagnosisChannelSummary(vertex,'vertex')+'\nPl@ntNet: '+diagnosisChannelSummary(plantnet,'plantnet')}
function refreshDashboardPlantSummaries(){for(let camera=1;camera<=6;camera++)updateDashboardPlantSummary(camera)}
window.updateDashboardPlantSummary=updateDashboardPlantSummary;
window.refreshDashboardPlantSummaries=refreshDashboardPlantSummaries;
window.refreshDashboardPlantSummaries();
})();
