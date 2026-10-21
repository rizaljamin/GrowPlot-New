/* Deterministic HSV colour screening shared by GrowPlot diagnosis stages. */
(function(root){
  'use strict';
  function classifyRgb(r,g,b){
    r=Math.max(0,Math.min(255,Number(r)||0))/255;
    g=Math.max(0,Math.min(255,Number(g)||0))/255;
    b=Math.max(0,Math.min(255,Number(b)||0))/255;
    const max=Math.max(r,g,b),min=Math.min(r,g,b),delta=max-min;
    let hue=0;
    if(delta){
      if(max===r)hue=60*(((g-b)/delta)%6);
      else if(max===g)hue=60*((b-r)/delta+2);
      else hue=60*((r-g)/delta+4);
    }
    if(hue<0)hue+=360;
    const saturation=max===0?0:delta/max,value=max;
    if(value<0.12||saturation<0.18)return 0;
    if(hue>=58&&hue<=170&&value>=0.16)return 1;
    if(hue>=34&&hue<58&&value>=0.30)return 2;
    if(hue>=8&&hue<40&&value<=0.68)return 3;
    return 0;
  }
  function analyzeRgba(data){
    const total=Math.floor((data&&data.length||0)/4);
    let green=0,yellow=0,brown=0;
    for(let i=0;i<total;i++){
      const k=i*4,category=classifyRgb(data[k],data[k+1],data[k+2]);
      if(category===1)green++;else if(category===2)yellow++;else if(category===3)brown++;
    }
    const percent=n=>total?Math.round(n*1000/total)/10:0;
    const classified=green+yellow+brown;
    return {totalPixels:total,greenPixels:green,yellowPixels:yellow,brownPixels:brown,
      otherPixels:Math.max(0,total-classified),green:total?green/total:0,
      yellow:total?yellow/total:0,brown:total?brown/total:0,
      other:total?Math.max(0,total-classified)/total:0,
      greenPercent:percent(green),yellowPercent:percent(yellow),
      brownPercent:percent(brown),otherPercent:percent(Math.max(0,total-classified)),
      method:'HSV threshold screening'};
  }
  const api={classifyRgb,analyzeRgba};
  root.GrowPlotHSV=api;
  if(typeof module==='object'&&module.exports)module.exports=api;
})(typeof window==='object'?window:globalThis);
