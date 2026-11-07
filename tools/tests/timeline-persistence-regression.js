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
assert(!java.includes('TimelineBuckets.latestFourPhotoIndices(captureTimes)'),'daily timeline must not choose four individual files when some share the same day');
assert(java.includes('TimelineBuckets.latestSavedPeriods(timelineInterval, captureTimes)'),'all interval modes must group saved photos before selecting');
assert(java.includes('TimelineBuckets.latestPhotoIndices(periods, captureTimes)'),'weekly, monthly, and yearly history should keep calendar-period selection');
assert(html.includes('Latest 4 distinct saved days'),'Daily view should explicitly describe grouping by distinct saved dates');
assert(html.includes('No saved photos found'),'empty daily history should not show sample photos or blank calendar dates');
assert(java.indexOf('if (treeUri != null) {',java.indexOf('private synchronized void scanSnapshotFolder(String requestedPlotId)'))<java.indexOf('else if (apiFolderId != null',java.indexOf('private synchronized void scanSnapshotFolder(String requestedPlotId)')),'configured Drive path should take precedence over a stale cached API folder');

// The Drive scanner must list metadata before selecting photo timestamps and
// downloading JPEG bytes. Date filtering belongs after discovery, never in listing.
const drivePhase=java.slice(
    java.indexOf('private List<FeedDocument> queryDriveImageFeed('),
    java.indexOf('private void uploadDriveApiPhoto(')
);
const listingPhase=drivePhase.slice(
    drivePhase.indexOf('private void collectDriveImageFeed('),
    drivePhase.indexOf('private long parseDriveModifiedAt(')
);
assert(drivePhase.includes('List<DriveFeedCandidate> discovered = new ArrayList<>()'),
    'Drive scan should first build an inventory of saved photo metadata');
assert(drivePhase.indexOf('collectDriveImageFeed(folderId, discovered')
    < drivePhase.indexOf('driveApiRequest("GET"'),
    'Drive listing must finish before photos are downloaded');
assert(!listingPhase.includes('driveApiRequest(')
    && !listingPhase.includes('openInputStream('),
    'discovery must not open, decode, or download JPEGs');
assert(!listingPhase.includes('System.currentTimeMillis()')
    && !listingPhase.includes('latestFour('),
    'discovery must not compare a saved photo against today');
assert(drivePhase.includes('group.sort((left, right) ->')
    && drivePhase.includes('Long.compare(left.timestamp, right.timestamp)'),
    'sort discovered image metadata by timestamp before choosing the latest');
assert(!drivePhase.includes('latestFourPhotoIndices(captureTimes)')
    && drivePhase.includes('latestSavedPeriods(timelineInterval, captureTimes)')
    && drivePhase.includes('latestPhotoIndices(periods, captureTimes)'),
    'all interval modes must group files by saved date/week/month before selecting one latest photo');
assert(drivePhase.includes('indices.add(group.size() - 1)'),
    'preserve the latest frame for the Home snapshot');
assert(java.includes('document.modifiedAt > 0')
    && java.includes('candidate.modifiedAt'),
    'preserve actual Google Drive modifiedTime for files lacking an embedded capture date');
assert(drivePhase.includes('catch (Exception downloadFailure)'),
    'one inaccessible JPEG must not abort downloads for other saved photos');

// Demo Plot fixture: five CCTV01 snapshots from 8–10 Oct 2026.
// The current date must not affect the ordering.
const samplePhotos=[
    'CCTV01_20261008_184621.jpg',
    'CCTV01_2026100800.jpg',
    'CCTV01_20261009_104256.jpg',
    'CCTV01_20261010_071851.jpg',
    'CCTV01_20261010_103821_399.jpg'
];
const timestamp=name=>{
    const match=name.match(/(\d{8})[_-]?(\d{6})/);
    if(match)return Number(match[1]+match[2]);
    const legacy=name.match(/(\d{8})[_-]?(\d{2})(?!\d)/);
    return legacy?Number(legacy[1]+legacy[2]+'0000'):0;
};
const latest=samplePhotos.map(name=>({name,ts:timestamp(name)}))
    .sort((a,b)=>a.ts-b.ts).slice(-4).map(photo=>photo.name);
assert.deepStrictEqual(latest,[
    'CCTV01_20261008_184621.jpg',
    'CCTV01_20261009_104256.jpg',
    'CCTV01_20261010_071851.jpg',
    'CCTV01_20261010_103821_399.jpg'
], 'source file inventory is ordered without referencing the current date');

// CCTV01 user fixture: 8 files span FIVE distinct non-consecutive dates.
// Cached slider must show the newest photo from each of the latest FOUR dates,
// not the newest four files (which would incorrectly double up 1 and 2 Nov).
const sourceNames=[
 'CCTV01_20261026_145157_875.jpg',
 'CCTV01_20261031_083455_524.jpg',
 'CCTV01_20261031_124107_254.jpg',
 'CCTV01_20261101_222210_303.jpg',
 'CCTV01_20261101_224509_801.jpg',
 'CCTV01_20261102_165900_773.jpg',
 'CCTV01_20261102_211548_437.jpg',
 'CCTV01_20261104_100441_273.jpg'
];
const localTimestamp=name=>{
 const m=name.match(/(\\d{8})_(\\d{6})/);
 assert(m, 'photo fixture includes sortable timestamp: '+name);
 const d=m[1],t=m[2];
 return Date.parse(d.slice(0,4)+'-'+d.slice(4,6)+'-'+d.slice(6,8)+'T'
    +t.slice(0,2)+':'+t.slice(2,4)+':'+t.slice(4,6)+'+08:00');
};
const dayTimeline=api.emptyTimeline(1,'daily');
dayTimeline.slots=sourceNames.map((name,i)=>({
 available:true,imageUrl:'saved-'+i+'.jpg',timestamp:localTimestamp(name),
 capturedAt:name,expectedDate:name
}));
api.saveCameraTimeline('plot-1','daily',[dayTimeline]);
const normalized=api.loadCameraTimeline('plot-1','daily')[0].slots;
assert.strictEqual(normalized.length,4,'four distinct saved days should produce four slides');
assert.deepStrictEqual(Array.from(normalized,photo=>photo.capturedAt),[
 'CCTV01_20261031_124107_254.jpg',
 'CCTV01_20261101_224509_801.jpg',
 'CCTV01_20261102_211548_437.jpg',
 'CCTV01_20261104_100441_273.jpg'
], 'pick the newest photo per date, skip gaps and show four newest dates chronologically');
const newestCache=localTimestamp('CCTV01_20261104_100441_273.jpg');
const olderSameDay=localTimestamp('CCTV01_20261104_094441_273.jpg');
api.addCachedCurrentPhotos('plot-1','daily',[{camera:1,timestamp:olderSameDay}],api.loadCameraTimeline('plot-1','daily'));
const deduped=api.loadCameraTimeline('plot-1','daily')[0].slots;
assert.strictEqual(deduped.length,4,'cached same-day photo must not create a fifth day');
assert.strictEqual(deduped[3].timestamp,newestCache,'earlier same-day cached photo must not replace the latest photograph');
assert(java.includes('long timestampInName = timestampFromFileName(name)'),'metadata stage must sort by capture timestamp in filename when present');
assert(java.includes('if (timestampInName <= 0) photo.capturedAt = parsed.getTime()'),
 'EXIF timestamp must not override a filename timestamp after Drive sorting');

console.log('PASS: CCTV photo history groups distinct saved dates and selects the latest in each.');
