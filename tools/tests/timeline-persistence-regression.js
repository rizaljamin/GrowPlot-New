const fs=require('fs'),vm=require('vm'),assert=require('assert');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
const source=html.slice(html.indexOf('const timelineModes='),html.indexOf('const defaultPlots='));
const values=new Map();
function context(){const ctx={plantIds:Array(6).fill({}),localStorage:{getItem:key=>values.has(key)?values.get(key):null,setItem:(key,value)=>values.set(key,value)},formatCapture:()=>({date:'today',time:'now'}),window:{GrowPlotBridge:{getTimelineInterval:()=> 'daily'}}};vm.createContext(ctx);vm.runInContext(source+';globalThis.timelineApi={loadCameraTimeline,saveCameraTimeline,emptyTimeline,timelineModes,addCachedCurrentPhotos}',ctx);return ctx.timelineApi}
let api=context();
const daily=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,'daily'));
daily[1].slots[3]={label:'Day 4',expectedDate:'21 Oct 2026',available:true,imageUrl:'https://appassets.androidplatform.net/assets/history/plot-2/cctv_02_1792584000000_ab12.jpg',capturedAt:'21 Oct 2026'};
api.saveCameraTimeline('plot-2','daily',daily);
api=context();
assert.strictEqual(api.loadCameraTimeline('plot-2','daily')[1].slots[3].imageUrl,daily[1].slots[3].imageUrl,'daily timeline should restore after a page reload');
assert.strictEqual(api.loadCameraTimeline('plot-2','weekly')[1].slots[3].available,false,'weekly timeline should keep its own interval data');
assert.strictEqual(api.loadCameraTimeline('plot-1','daily')[1].slots[3].available,false,'timeline cache must be scoped to the selected plot');
for(const mode of ['daily','weekly','monthly','yearly']){const timeline=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,mode));assert(api.addCachedCurrentPhotos('plot-2',mode,[{camera:2,timestamp:Date.now()}],timeline),'latest cached photo should populate the current period when history is unavailable');assert(timeline[1].slots[3].available&&timeline[1].slots[3].imageUrl.includes('/assets/images/plot-2/cctv_02.jpg'),'fallback should use the local cached camera image');assert.strictEqual(timeline[1].slots[0].available,false,'fallback must not claim a photo for older periods');}
const scan=html.slice(html.indexOf('window.onSnapshotScan=function'),html.indexOf("if(window.GrowPlotBridge){document.getElementById('refreshInterval')"));
assert(scan.includes('saveCameraTimeline(selectedPlotId,timelineInterval,cameraTimeline)'),'successful scans should persist the selected interval timeline');
assert(scan.includes('Array.isArray(data.timelinePhotos)'),'status-only scans should leave the saved timeline intact');
assert(html.includes('cameraTimeline=loadCameraTimeline(selectedPlotId,value)'),'switching intervals should restore that interval while its scan runs');
console.log('PASS: interval timelines restore after reload and remain isolated by plot and interval.');
