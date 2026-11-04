/* Firebase Demo Plot preview: never load previously saved on-device camera IPs. */
window.GrowPlotApplyDemoStreamConfig=function(data){
 if(!data||data.greenhouseId!=='greenhouse-1')return;
 const count=Number(data.sectionsPerGreenhouse);
 if(!Number.isInteger(count)||count<0||count>100)return;
 window.GrowPlotDemoSectionCount=count;
 const labels={};
 for(const stream of (Array.isArray(data.streams)?data.streams:[])){
  const m=/^section-([1-3])$/.exec(String(stream.sectionId||''));
  if(m&&/^[A-Za-z0-9_-]{1,100}$/.test(String(stream.deviceId||'')))labels[Number(m[1])]=stream.deviceId;
 }
 window.GrowPlotDemoCameraLabels=labels;
 if(document.getElementById('plot')?.value==='plot-1')render();
};
window.GrowPlotDemoStreamUnavailable=function(){
 window.GrowPlotDemoCameraLabels={};
 if(document.getElementById('plot')?.value==='plot-1'){
  render();
  showSaveToast('Demo Plot Firebase streams unavailable. No local IP fallback.');
 }
};
