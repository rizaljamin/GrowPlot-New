'use strict';
const assert=require('node:assert/strict');
const fs=require('node:fs');
const {pathToFileURL}=require('node:url');
const path=require('node:path');
(async()=>{
 const file=pathToFileURL(path.resolve('functions/demo-streams.js')).href;
 const {projectDemoStreamConfig}=await import(file);
 const plot={id:'plot-1',greenhouseCount:1,sectionsPerGreenhouse:1};
 const assignments=[
  {plotId:'plot-1',greenhouseId:'greenhouse-1',sectionId:'section-1',deviceId:'CCTV03',ownerUserId:'owner-abc'},
  {plotId:'plot-1',greenhouseId:'greenhouse-1',sectionId:'section-2',deviceId:'CCTV02',ownerUserId:'owner-abc'},
  {plotId:'plot-1',greenhouseId:'greenhouse-1',sectionId:'section-3',deviceId:'CCTV01',ownerUserId:'owner-abc'},
  {plotId:'private',greenhouseId:'greenhouse-1',sectionId:'section-1',deviceId:'CCTV99',ownerUserId:'owner-other'}
 ];
 const device=(deviceId,ipAddress)=>({deviceId,ownerUserId:'owner-abc',deviceType:'AV',protocol:'HTTP',port:8080,streamPath:'/videofeed',ipAddress,username:'PRIVATE_USER',password:'PRIVATE_PASSWORD',credentials:{ciphertext:'PRIVATE_CIPHERTEXT'}});
 const devices={
  'owner-abc_CCTV03':device('CCTV03','192.168.0.7'),
  'owner-abc_CCTV02':device('CCTV02','192.168.0.8'),
  'owner-abc_CCTV01':device('CCTV01','192.168.0.9')
 };
 const result=projectDemoStreamConfig(plot,assignments,devices);
 assert.equal(result.sectionsPerGreenhouse,1,'Demo count comes from Firestore plot configuration');
 assert.equal(result.streams.length,1,'one section activates one linked camera');
 assert.equal(result.streams[0].sectionId,'section-1');
 assert.equal(result.streams[0].deviceId,'CCTV03');
 assert.equal(result.streams[0].streamUrl,'http://192.168.0.7:8080/videofeed','stream route is from the assigned CCTV03, not legacy slot 1');
 assert.doesNotMatch(JSON.stringify(result),/PRIVATE_|owner-abc/,'response does not disclose private credentials or owner IDs');
 const more=projectDemoStreamConfig({...plot,sectionsPerGreenhouse:3},assignments,devices);
 assert.equal(more.streams.length,3,'additional sections are available only when Firestore config enables them');
 assert.deepEqual(more.streams.map(x=>x.deviceId),['CCTV03','CCTV02','CCTV01']);
 const missing=projectDemoStreamConfig({...plot,sectionsPerGreenhouse:1},[],devices);
 assert.equal(missing.streams.length,0,'no linked CCTV produces no legacy fallback');
 const invalid=projectDemoStreamConfig(plot,assignments,{'owner-abc_CCTV03':{...devices['owner-abc_CCTV03'],ipAddress:'evil.com@host'}});
 assert.equal(invalid.streams.length,0,'invalid camera URL is excluded');
 const java=fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java','utf8');
 assert.match(java,/getHttpsCallable\("publicDemoStreamConfig"\)/,'native Demo streams use Firebase callable');
 assert.match(java,/if \("plot-1"\.equals\(plotId\) && "greenhouse-1"\.equals\(greenhouseId\)\)/,'Demo caller independent of login');
 assert.match(java,/loadAssignedStreams\("plot-1", "greenhouse-1", true\)/,'return to Demo refreshes Firebase, not local IPs');
 assert.doesNotMatch(java,/userStreamMode = false/,'all Home streaming paths fail closed without Firebase');
 assert.doesNotMatch(java,/return ip\.isEmpty\(\) \? "" : "http:\/\/" \+ ip/,'legacy local-IP stream fallback is removed');
 assert.match(java,/const absent='0,0,1,1,1,0'/,'obsolete native overlays are hidden');
 const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
 const ui=fs.readFileSync('app/src/main/assets/demo-stream-ui.js','utf8');
 assert.match(html,/src="demo-stream-ui\.js"/,'the Firebase Demo UI handler is loaded from an external asset');
 assert.match(ui,/window\.GrowPlotApplyDemoStreamConfig=function\(data\)/,'Home processes Firebase section metadata');
 assert.match(html,/for\(let i=1;i<=total;i\+\+\)/,'Home renders actual configured section count');
 assert.match(html,/const poster=demo&&i<=3\?''/,'Demo never displays local stream as a poster');
 const functions=fs.readFileSync('functions/index.js','utf8');
 assert.match(functions,/export const publicDemoStreamConfig=onCall/,'cloud callable deployed');
 assert.match(functions,/db\.collection\('feed_assignments'\)\.where\('plotId','==','plot-1'\)/,'callable only queries shared Demo assignments');
 const rules=fs.readFileSync('firestore.rules','utf8');
 assert.match(rules,/request\.resource\.data\.sectionsPerGreenhouse >= 1/,'verified Demo admin can save one section');
 console.log('PASS: Demo Plot config and CCTV03 Section 01 from Firebase; logged-out fallback removed; credentials excluded.');
})().catch(e=>{console.error(e);process.exitCode=1});
