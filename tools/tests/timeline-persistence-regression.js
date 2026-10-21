const fs=require('fs'),vm=require('vm'),assert=require('assert');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
const java=fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java','utf8');
const source=html.slice(html.indexOf('const timelineModes='),html.indexOf('const defaultPlots='));
const values=new Map();
function context(user='user-1'){const ctx={plantIds:Array(6).fill({}),snapshotTimes:Array.from({length:6},(_,i)=>({iso:'2026-10-08T18:46:'+String(21+i*4).padStart(2,'0')+'+08:00'})),localStorage:{getItem:key=>values.has(key)?values.get(key):null,setItem:(key,value)=>values.set(key,value)},formatCapture:()=>({date:'today',time:'now'}),window:{GrowPlotBridge:{getFeedUserScope:()=>user,getTimelineInterval:()=> 'daily'}}};ctx.GrowPlotBridge=ctx.window.GrowPlotBridge;vm.createContext(ctx);vm.runInContext(source+';globalThis.timelineApi={loadCameraTimeline,saveCameraTimeline,emptyTimeline,timelineModes,addCachedCurrentPhotos,timelineStorageKey,storedCurrentPhotos}',ctx);return ctx.timelineApi}
let api=context();
const daily=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,'daily'));
daily[1].slots[3]={label:'Day 4',expectedDate:'21 Oct 2026',available:true,imageUrl:'https://appassets.androidplatform.net/assets/history/plot-2/cctv_02_1792584000000_ab12.jpg',capturedAt:'21 Oct 2026'};
api.saveCameraTimeline('plot-2','daily',daily);
api=context();
assert.strictEqual(api.loadCameraTimeline('plot-2','daily')[1].slots[3].imageUrl,daily[1].slots[3].imageUrl,'daily timeline should restore for the same signed-in user');
assert.strictEqual(api.loadCameraTimeline('plot-2','weekly')[1].slots[3].available,false,'weekly timeline should keep its own interval data');
assert.strictEqual(api.loadCameraTimeline('plot-1','daily')[1].slots[3].available,false,'timeline cache must be scoped to the selected plot');
assert.strictEqual(context('user-2').loadCameraTimeline('plot-2','daily')[1].slots[3].available,false,'another user must not see this user’s plot history');
for(const mode of ['daily','weekly','monthly','yearly']){const timeline=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,mode));assert(api.addCachedCurrentPhotos('plot-2',mode,[{camera:2,timestamp:Date.now()}],timeline),'latest selected-plot photo should populate the current period');assert(timeline[1].slots[3].available&&timeline[1].slots[3].imageUrl.includes('/assets/images/plot-2/cctv_02.jpg'),'cached photo should use this plot’s local image');assert.strictEqual(timeline[1].slots[0].available,false,'cached current photo must not claim older periods');}
for(const mode of ['daily','weekly','monthly','yearly']){const timeline=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,mode)),timestamp=Date.parse('2026-10-08T18:46:37+08:00');assert(api.addCachedCurrentPhotos('plot-2',mode,[{camera:2,timestamp}],timeline),'cached photo should anchor a period to its actual capture date');const chosen=timeline[1].slots.find(slot=>slot.available);assert(chosen,'dated photo should appear in the interval timeline');assert.strictEqual(chosen.expectedDate,mode==='daily'?'08 Oct 2026':mode==='weekly'?'05 Oct 2026':mode==='monthly'?'01 Oct 2026':'01 Jan 2026','photo should be placed in its actual day/week/month/year bucket');}
const empty=Array.from({length:6},(_,i)=>api.emptyTimeline(i+1,'daily'));
assert.deepStrictEqual(JSON.parse(JSON.stringify(empty)),JSON.parse(JSON.stringify(api.loadCameraTimeline('plot-pp5','daily'))),'a plot with no saved feed photos must remain empty instead of receiving bundled Demo photos');
assert(!source.includes('suppliedBaselineTimelines'),'timeline code must not inject bundled sample images into a user plot');
assert(html.includes("plot.id==='plot-1'&&/^demo plot$/i.test(plot.name||'')")&&html.includes('Demo photos and findings are not used for this plot.'),'bundled sample findings must only appear for the explicitly selected Demo plot');
assert(java.includes('return user == null ? "signed-out" : user.getUid();'),'timeline local storage must use the signed-in Firebase user scope');
assert(java.includes('data_feed_tree_uri_" + currentFeedUserKey() + "_" + safePlotId(plotId)'),'Drive tree grants must be scoped to user and plot');
assert(java.includes('String userFolder = signedInEmailFolderName();')&&java.includes('findOrCreateChildFolder(treeUri, imageFeedId, userFolder)')&&java.includes('findOrCreateChildFolder(treeUri, userFolderId, plotFolder)'),'default feed path must resolve Image_Feed / signed-in email / selected plot');
const scan=java.slice(java.indexOf('private synchronized void scanSnapshotFolder(String requestedPlotId)'),java.indexOf('private List<FeedDocument> queryImageFeedFolder'));
assert(scan.includes('getPrefs().getString(feedTreeKey(plotId), null)'),'scan must use the selected user and plot folder mapping');
assert(!scan.includes('PREF_SELECTED_URIS')&&!scan.includes('PREF_IMAGE_FEED_TREE_URI'),'scan must not fall back to a global folder or another plot’s selected images');
assert(java.includes('currentFeedUserKey() + "/" + safePlotId(best.plotId) + "/" + best.historyFile'),'archived timeline image URLs must carry both user and plot scope');
assert(html.includes("localStorage.setItem('growplotFeedSummary:'+encodeURIComponent(activeFeedUserScope())+':'+selectedPlotId"),'current photo caches must be scoped to user and plot');
console.log('PASS: plot timelines and feed caches are user/plot scoped, and empty timelines never receive Demo baseline photos.');
