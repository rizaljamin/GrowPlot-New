'use strict';
const fs=require('node:fs');
const assert=require('node:assert/strict');
const vm=require('node:vm');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
const account=fs.readFileSync('app/src/main/assets/account-login.js','utf8');
const dropdown=fs.readFileSync('app/src/main/assets/plot-dropdown.js','utf8');
const bridge=fs.readFileSync('app/src/main/java/com/growplot/farm/AccountApiBridge.java','utf8');

// Execute the production plot identity filter against mixed owners and duplicate IDs.
const begin=html.indexOf('function plotPickerRecords(');
const end=html.indexOf('function fillPlotSelectors(',begin);
assert.ok(begin>=0&&end>begin,'canonical plot picker function exists');
const context=vm.createContext({});
vm.runInContext(html.slice(begin,end),context);
const records=[
 {id:'plot-1',number:'1',name:'Demo Plot'},
 {id:'plot-1',number:'1',name:'Duplicated local demo'},
 {id:'user-a-gesing',number:'1',name:'Gesing',ownerUserId:'uid-a'},
 {id:'user-b-demo',number:'1',name:'Demo Plot',ownerUserId:'uid-b'},
 {id:'user-a-pp5',number:'2',name:'PP5',ownerUserId:'uid-a'},
 {id:'user-a-gesing',number:'1',name:'Duplicated Firebase row',ownerUserId:'uid-a'}
];
const owned=context.plotPickerRecords(records,'uid-a',true).map(p=>p.id);
assert.deepEqual(Array.from(owned),['plot-1','user-a-gesing','user-a-pp5'],
 'Settings options contain one canonical demo and distinct user-owned plot IDs');
assert.equal(new Set(owned).size,owned.length,'no duplicate option IDs');
assert.match(html,/plotNumbers\.get\(String\(p\.number\|\|''\)\)/,'colliding displayed plot numbers are detected');
assert.match(html,/\[ID: /,'duplicate plot numbers show the distinct underlying plot IDs');
assert.match(account,/seenPlotIds\.has\(id\)\|\|String\(p\.ownerUserId\|\|'\'\)!==uid/,
 'Firebase profile excludes repeated/cross-account plot IDs');
assert.match(dropdown,/String\(p\.ownerUserId\|\|'\'\)!==uid/,
 'Home owner filter agrees with Settings');
assert.match(bridge,/\.whereEqualTo\("ownerUserId",user\.getUid\(\)\)/,
 'native Firebase profile uses an owner-filtered query');

// Run the actual auto-selection function with fake saved Firebase assignments.
const start=account.indexOf('window.GrowPlotSelectAssignedCamera=()=>{');
const stop=account.indexOf('window.GrowPlotUnlinkedNotices=',start);
assert.ok(start>=0&&stop>start,'automatic camera-selection callback exists');
const fn=account.slice(start,stop);
let switches=[];
const ctx=vm.createContext({
 window:{
  GrowPlotOwnedDeviceData:{
   assignments:[{plotId:'plot-1',greenhouseId:'greenhouse-1',sectionId:'section-1',deviceId:'CCTV03'}],
   devices:[{deviceId:'CCTV03',ipAddress:'192.168.0.3'}]
  },
  GrowPlotRefreshUnlinkButton:()=>{}
 },
 anchor:{value:'plot-1'},ghSelect:{value:'greenhouse-1'},secSelect:{value:'section-1'},
 manuallySelectedCamera:true,cameraFormDirty:false,
 selectSavedDevice:(d)=>switches.push(d.deviceId)
});
vm.runInContext(fn,ctx);
vm.runInContext('window.GrowPlotSelectAssignedCamera()',ctx);
assert.deepEqual(switches,[],'manual CCTV02 selection survives background CCTV03 assignment refresh');
ctx.manuallySelectedCamera=false;ctx.cameraFormDirty=true;
vm.runInContext('window.GrowPlotSelectAssignedCamera()',ctx);
assert.deepEqual(switches,[],'unsaved IP edit survives Firebase refresh');
ctx.cameraFormDirty=false;
vm.runInContext('window.GrowPlotSelectAssignedCamera()',ctx);
assert.deepEqual(switches,['CCTV03'],'automatic section-linked selection runs when no manual/dirty edit');
assert.match(account,/manualCameraId=devicePicker\.value/,'manual camera ID is remembered');
assert.match(account,/const previous=devicePicker\.value\|\|manualCameraId/,
 'inventory reload preserves manual selection');
assert.doesNotMatch(account,/#manageFarmModal,#growplotOwnedDevices/,
 'clicking in camera settings cannot snap back to Home plot');
assert.match(account,/window\.GrowPlotLastSavedDeviceId=deviceId/,
 'save targets exact selected camera');
assert.match(account,/getElementById\('gpDeviceId'\)\?\.value===savedId/,
 'async save response only clears the saved camera form');
console.log('PASS: plot ownership/identity, CCTV selection lock, unsaved IP preservation, and save identity.');
