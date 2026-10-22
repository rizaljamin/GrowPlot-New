/* Deterministic HSV colour screening shared by GrowPlot diagnosis stages. */
(function(root){
  'use strict';
  function rgbToHsv(r,g,b){
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
    return {hue,saturation:max===0?0:delta/max,value:max};
  }
  function classifyHsv(hue,saturation,value){
    if(value<0.12||saturation<0.18)return 0;
    if(hue>=58&&hue<=170&&value>=0.16)return 1;
    if(hue>=34&&hue<58&&value>=0.30)return 2;
    if(hue>=8&&hue<40&&value<=0.68)return 3;
    return 0;
  }
  function classifyRgb(r,g,b){
    const hsv=rgbToHsv(r,g,b);
    return classifyHsv(hsv.hue,hsv.saturation,hsv.value);
  }
  function analyzeRgba(data,width,height){
    const total=Math.floor((data&&data.length||0)/4);
    width=Math.max(0,Math.floor(Number(width)||0));
    height=Math.max(0,Math.floor(Number(height)||0));
    let green=0,yellow=0,brown=0,graySum=0,graySquareSum=0,saturationSum=0;
    const labels=new Uint8Array(total),gray=new Uint8Array(total),hueChannel=new Uint8ClampedArray(total),saturationChannel=new Uint8ClampedArray(total),valueChannel=new Uint8ClampedArray(total);
    for(let i=0;i<total;i++){
      const k=i*4,r=data[k],g=data[k+1],b=data[k+2],hsv=rgbToHsv(r,g,b),category=classifyHsv(hsv.hue,hsv.saturation,hsv.value);
      hueChannel[i]=Math.round(hsv.hue*255/360);saturationChannel[i]=Math.round(hsv.saturation*255);valueChannel[i]=Math.round(hsv.value*255);
      const luminance=Math.round(.299*r+.587*g+.114*b),max=Math.max(r,g,b),min=Math.min(r,g,b);
      labels[i]=category;gray[i]=luminance;graySum+=luminance;graySquareSum+=luminance*luminance;
      saturationSum+=max?((max-min)/max):0;
      if(category===1)green++;else if(category===2)yellow++;else if(category===3)brown++;
    }
    const percent=n=>total?Math.round(n*1000/total)/10:0;
    const classified=green+yellow+brown;
    let edgePixels=0,edgeTotal=0;
    if(width>2&&height>2&&width*height<=total){
      for(let y=0;y<height-1;y++)for(let x=0;x<width-1;x++){
        const p=y*width+x;
        const gx=Math.abs(gray[p+1]-gray[p]),gy=Math.abs(gray[p+width]-gray[p]);
        if(Math.max(gx,gy)>=36)edgePixels++;
        edgeTotal++;
      }
    }
    const meanBrightness=total?graySum/total:0;
    return {totalPixels:total,greenPixels:green,yellowPixels:yellow,brownPixels:brown,
      otherPixels:Math.max(0,total-classified),green:total?green/total:0,
      yellow:total?yellow/total:0,brown:total?brown/total:0,
      other:total?Math.max(0,total-classified)/total:0,
      greenPercent:percent(green),yellowPercent:percent(yellow),
      brownPercent:percent(brown),otherPercent:percent(Math.max(0,total-classified)),
      meanBrightness:Math.round(meanBrightness*10)/10,
      brightnessContrast:Math.round((total?Math.sqrt(Math.max(0,graySquareSum/total-meanBrightness*meanBrightness)):0)*10)/10,
      meanSaturationPercent:Math.round((total?saturationSum*100/total:0)*10)/10,
      edgeDensityPercent:edgeTotal?Math.round(edgePixels*1000/edgeTotal)/10:0,
      labels,hueChannel,saturationChannel,valueChannel,
      method:'HSV threshold screening with grayscale contrast and edge-density checks'};
  }
  function connectedComponents(labels,width,height,minArea,limit){
    width=Math.max(0,Math.floor(Number(width)||0));height=Math.max(0,Math.floor(Number(height)||0));
    const total=width*height;if(!labels||labels.length<total||!total)return [];
    minArea=Math.max(1,Math.floor(Number(minArea)||1));limit=Math.max(1,Math.floor(Number(limit)||12));
    const seen=new Uint8Array(total),queue=new Int32Array(total),regions=[];
    for(let n=0;n<total;n++){
      if(!labels[n]||seen[n])continue;
      let head=0,tail=1,area=0,xmin=width,ymin=height,xmax=0,ymax=0,green=0,yellow=0,brown=0;
      queue[0]=n;seen[n]=1;
      while(head<tail){
        const p=queue[head++],x=p%width,y=(p/width)|0,kind=labels[p];area++;
        if(kind===1)green++;else if(kind===2)yellow++;else if(kind===3)brown++;
        if(x<xmin)xmin=x;if(x>xmax)xmax=x;if(y<ymin)ymin=y;if(y>ymax)ymax=y;
        let q;
        if(x>0){q=p-1;if(labels[q]&&!seen[q]){seen[q]=1;queue[tail++]=q}}
        if(x+1<width){q=p+1;if(labels[q]&&!seen[q]){seen[q]=1;queue[tail++]=q}}
        if(y>0){q=p-width;if(labels[q]&&!seen[q]){seen[q]=1;queue[tail++]=q}}
        if(y+1<height){q=p+width;if(labels[q]&&!seen[q]){seen[q]=1;queue[tail++]=q}}
      }
      if(area>=minArea)regions.push({area,xmin,ymin,xmax,ymax,green,yellow,brown});
    }
    regions.sort((a,b)=>b.area-a.area);
    return regions.slice(0,limit);
  }
  const api={classifyRgb,analyzeRgba,connectedComponents};
  root.GrowPlotHSV=api;
  if(typeof module==='object'&&module.exports)module.exports=api;
})(typeof window==='object'?window:globalThis);
