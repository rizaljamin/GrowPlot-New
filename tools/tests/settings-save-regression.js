const fs=require('fs'),vm=require('vm'),assert=require('assert');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
const code=html.slice(html.indexOf('function saveAllSettings(){'),html.indexOf("document.getElementById('saveCctvIps').addEventListener"));
function fixture(){
 const fields={plotManagerSelect:'plot-1',plotNumber:'1',plotName:'Updated farm',plotLatitude:'3.1',plotLongitude:'101.2',plotAddress:'Farm',plotDescription:'Crop notes',cctvSnapshotInterval:'2',refreshInterval:'15',vigiCctv03Username:'owner',vigiCctv03Password:''};
 for(let i=1;i<=6;i++)fields['cctvIp'+i]=i<=3?'192.168.1.'+i:'';
 const elements=Object.fromEntries(Object.entries(fields).map(([k,value])=>[k,{value}])),calls=[];
 const bridge={saveCctvCameraIpAddresses:ips=>{calls.push(['ips',JSON.parse(ips)]);return true},saveVigiCctv03Credentials:(u,p)=>{calls.push(['login',u,p]);return true},setCctvSnapshotIntervalMinutes:m=>calls.push(['snapshot',m]),setRefreshInterval:m=>calls.push(['rescan',m]),setActivePlot:id=>calls.push(['plot',id])};
 const ctx={document:{getElementById:id=>elements[id]||(elements[id]={})},plots:[{id:'plot-1',number:'1',name:'Old'}],selectedPlotId:'plot-1',window:{GrowPlotBridge:bridge,vigiCctv03HasPassword:true},GrowPlotBridge:bridge,showSaveToast(){},savePlotData:()=>true,fillPlotSelectors(){},defaultFeedFolder:()=> 'plot01_demo',updatePlotMapsLink(){},updateFeedScope(){},render(){}};
 vm.createContext(ctx);vm.runInContext(code,ctx);return {ctx,elements,calls,bridge};
}
let f=fixture();assert(f.ctx.saveAllSettings());assert.equal(f.ctx.plots[0].name,'Updated farm');assert(f.calls.some(c=>c[0]==='snapshot'&&c[1]===2));assert(f.calls.some(c=>c[0]==='rescan'&&c[1]===15));assert(f.calls.some(c=>c[0]==='login'&&c[2]===''));assert.equal(f.elements.allSettingsSaveStatus.textContent,'All settings saved successfully.');
f=fixture();f.elements.plotLatitude.value='95';assert(!f.ctx.saveAllSettings());assert.equal(f.calls.length,0,'Validate before writing settings');
f=fixture();f.bridge.saveVigiCctv03Credentials=()=>false;assert(!f.ctx.saveAllSettings());assert(!f.elements.allSettingsSaveStatus.textContent.includes('All settings saved'));
f=fixture();f.ctx.savePlotData=()=>false;assert(!f.ctx.saveAllSettings());assert.equal(f.ctx.plots[0].name,'Old');
assert(html.indexOf('id="saveAllSettingsButton"')<html.indexOf('id="moduleHomeButton"'));
console.log('PASS: combined save, saved-password reuse, validation before writes, partial-failure status and plot rollback.');
