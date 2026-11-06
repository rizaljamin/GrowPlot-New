const fs=require('fs'),vm=require('vm'),assert=require('assert');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
const java=fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java','utf8');
const source=html.slice(html.indexOf('const timelineModes='),html.indexOf('const defaultPlots='));
const values=new Map();
function context(){const ctx={plantIds:Array(6).fill({}),localStorage:{getItem:key=>values.has(key)?values.get(key):null,setItem:(key,value)=>values.set(key,value)},formatCapture:timestamp=>{const d=new Date(timestamp);return {date:new Intl.DateTimeFormat('en-GB',{timeZone:'Asia/Kuala_Lumpur',day:'2-digit',month:'short',year:'numeric'}).format(d).replaceAll(',',''),time:new Intl.DateTimeFormat('en-GB',{timeZone:'Asia/Kuala_Lumpur',hour:'2-digit',minute:'2-digit'}).format(d)}},window:{GrowPlotBridge:{getTimelineInterval:()=> 'daily'}}};vm.createContext(ctx);vm.runInContext(source+';globalThis.timelineApi={loadCameraTimeline,saveCameraTimeline,emptyTimeline,addCachedCurrentPhotos,applySuppliedBaselineIfEmpty}',ctx);return ctx.timelineApi}
let api=context();
const base=Date.parse('2026-10-01T12:00:00+08:00');
const daily=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,'daily'));
daily[1].slots=Array.from({length:6},(_,i)=>({available:true,imageUrl:'saved-'+i+'.jpg',timestamp:base+i*86400000,capturedAt:'capture '+i,expectedDate:'date '+i}));
api.saveCameraTimeline('plot-2','daily',daily);
api=context();
const restored=api.loadCameraTimeline('plot-2','daily')[1].slots;
assert.strictEqual(restored.length,4,'daily history should retain exactly the latest four saved photos');
assert.deepStrictEqual(Array.from(restored,slot=>slot.timestamp),[base+2*86400000,base+3*86400000,base+4*86400000,base+5*86400000],'daily history should sort saved photos by capture time, oldest to newest');
assert.strictEqual(api.loadCameraTimeline('plot-2','weekly')[1].slots.length,0,'weekly timeline should not show empty date windows without saved photos');
assert.strictEqual(api.loadCameraTimeline('plot-1','daily')[1].slots.length,0,'timeline cache must be scoped to the selected plot');
const cached=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,'daily'));
assert(api.addCachedCurrentPhotos('plot-2','daily',[{camera:2,timestamp:base+8*86400000}],cached),'a cached current photo should populate an otherwise empty daily history');
assert.strictEqual(cached[1].slots[0].timestamp,base+8*86400000,'cached daily photo should use its capture timestamp');
const privateEmpty=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,'daily'));
assert.strictEqual(api.applySuppliedBaselineIfEmpty('plot-1','daily',privateEmpty),privateEmpty,'photo history must not replace saved photos with bundled samples');
assert(java.includes('TimelineBuckets.latestFourPhotoIndices(captureTimes)'),'daily history should select the latest four saved photos');
assert(java.includes('TimelineBuckets.latestPhotoIndices(periods, captureTimes)'),'weekly, monthly, and yearly history should keep calendar-period selection');
assert(html.includes('Latest saved photos'),'Daily view should describe saved-photo selection');
assert(html.includes('No saved photos found'),'empty daily history should not show sample photos or blank calendar dates');
assert(java.indexOf('if (treeUri != null) {',java.indexOf('private synchronized void scanSnapshotFolder(String requestedPlotId)'))<java.indexOf('else if (apiFolderId != null',java.indexOf('private synchronized void scanSnapshotFolder(String requestedPlotId)')),'configured Drive path should take precedence over a stale cached API folder');

console.log('PASS: Daily history restores the latest four saved photos in capture-time order.');
