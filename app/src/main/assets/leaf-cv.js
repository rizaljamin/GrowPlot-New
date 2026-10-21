/* GrowPlot local CV screening, Build 06 candidate. No remote services or invented species diagnoses. */
(function(){'use strict';
 const css=`.cv-panel{margin:12px 0;padding:12px;border:1px solid #c8dfd0;border-radius:12px;background:#f6fbf7;color:#193e2b}.cv-panel h4{margin:0 0 7px;font-size:15px}.cv-panel p{font-size:12px;line-height:1.5;margin:6px 0}.cv-panel button{padding:9px 12px;border:0;border-radius:8px;background:#17553a;color:white;font-weight:700}.cv-preview{width:100%;max-height:400px;object-fit:contain;background:#162d20;border-radius:8px}.cv-metrics{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:5px;margin:8px 0}.cv-metrics span{padding:8px 4px;text-align:center;background:white;border:1px solid #d8e8dc;border-radius:7px;font-size:11px}.cv-metrics b{display:block;font-size:15px}.cv-caution{color:#8a5b17}.cv-thumbnails{display:flex;gap:6px;overflow-x:auto}.cv-leaf-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;margin:10px 0}.cv-leaf-card{background:white;border:1px solid #d8e8dc;border-radius:9px;padding:7px}.cv-leaf-card img{width:100%;height:100px;object-fit:contain;background:#edf3ee;border-radius:5px}.cv-leaf-card p{font-size:11px;margin:4px 0}.cv-flag{font-weight:700;color:#8b5918}.cv-ok{font-weight:700;color:#1e6a42}.cv-note{font-size:11px;color:#526659}.cv-panel small{display:block;line-height:1.45}`;
 document.head.insertAdjacentHTML('beforeend','<style>'+css+'</style>');
 function currentPanel(){return document.querySelector('#diagnosisModal .diagnosis-dialog')}
 function panel(camera){
  const inline=document.getElementById('growplotInlineResults');
  if(inline){
    inline.innerHTML='<p id="cvProgress" role="status">Preparing foliage analysis…</p><div id="cvResult"></div>';
    return inline;
  }
  let host=currentPanel();if(!host)return null;let el=document.getElementById('cvLeafPanel');if(!el){el=document.createElement('section');el.className='cv-panel';el.id='cvLeafPanel';const target=host.querySelector('#diagnosisDescription');if(target)target.insertAdjacentElement('beforebegin',el);else host.appendChild(el)}el.innerHTML='<h4>Plant image analysis · Section '+String(camera).padStart(2,'0')+'</h4><p>Run Diagnosis screens HSV colour cues on this device and attempts YOLO-World detection of broad plant parts through the diagnosis service. Plant-part boxes are not species identification or disease confirmation.</p><p id="cvProgress" role="status"></p><div id="cvResult"></div>';return el}
 function findPhoto(camera){try{const all=typeof diagnosisMediaFor==='function'?diagnosisMediaFor(camera):[];return all.find(x=>x.kind==='photo'&&x.url)}catch(e){return null}}
 function load(url){return new Promise((resolve,reject)=>{const im=new Image();im.onload=()=>resolve(im);im.onerror=()=>reject(Error('Image unavailable. Select or capture a local close-up.'));im.src=url})}
 function ensureHSV(){if(window.GrowPlotHSV)return Promise.resolve(window.GrowPlotHSV);if(window.__growPlotHSVLoading)return window.__growPlotHSVLoading;window.__growPlotHSVLoading=new Promise((resolve,reject)=>{const script=document.createElement('script');script.src='plant-hsv.js';script.onload=()=>window.GrowPlotHSV?resolve(window.GrowPlotHSV):reject(Error('plant-hsv.js loaded without initializing GrowPlotHSV'));script.onerror=()=>reject(Error('Unable to load local HSV analysis'));document.head.appendChild(script)}).catch(error=>{window.__growPlotHSVLoading=null;throw error});return window.__growPlotHSVLoading}
 function simplifyDiagnosisUi(){const modal=document.getElementById('diagnosisModal');if(!modal)return;modal.querySelector('#openCVAnalysisButton')?.remove();const identity=modal.querySelector('.species-confirm'),identityStatus=identity&&identity.querySelector('p');if(identityStatus&&identityStatus.textContent.includes('Visual estimate')){identityStatus.textContent='Preset estimate for this CCTV section only; it is not based on this photo. Run Diagnosis to get Pl@ntNet image-based species matches.';identity.querySelector('button[onclick^="confirmDiagnosisEstimate"]')?.remove()}modal.querySelectorAll('.diagnosis-analysis-note').forEach(note=>{if(note.textContent.includes('Photo screening')||note.textContent.includes('colour'))note.textContent='Run Diagnosis performs on-device HSV colour screening, attempts YOLO-World plant-part detection through the configured service, and asks Pl@ntNet to identify species and check disease candidates. YOLO locates broad plant parts; it does not diagnose disease.'});const estimate=modal.querySelector('.species-confirm');if(estimate){const status=estimate.querySelector('p');if(status&&status.textContent.includes('Visual estimate'))status.innerHTML='This is a preset visual estimate for the CCTV section, not an image-based species identification. Run Diagnosis to compare the submitted photos with Pl@ntNet.';estimate.querySelector('button[onclick^="confirmDiagnosisEstimate"]')?.remove()}const headings=modal.querySelectorAll('h4');headings.forEach(h=>{if(h.textContent.trim()==='2. CV diagnosis')h.textContent='2. Plant image analysis'})}
 async function screenUrl(url){const im=await load(url),side=112,scale=Math.min(side/im.naturalWidth,side/im.naturalHeight),w=Math.max(1,Math.round(im.naturalWidth*scale)),h=Math.max(1,Math.round(im.naturalHeight*scale)),canvas=document.createElement('canvas');canvas.width=w;canvas.height=h;const ctx=canvas.getContext('2d',{willReadFrequently:true});if(!ctx)throw Error('Canvas is unavailable');ctx.drawImage(im,0,0,w,h);return Object.assign({},window.GrowPlotHSV.analyzeRgba(ctx.getImageData(0,0,w,h).data),{colorSpace:'HSV',width:im.naturalWidth,height:im.naturalHeight})}
 window.screenDiagnosisImage=(url)=>ensureHSV().then(()=>screenUrl(url));
 async function analyze(camera){await ensureHSV();simplifyDiagnosisUi();const root=panel(camera);if(!root)return;const status=root.querySelector('#cvProgress'),result=root.querySelector('#cvResult'),item=findPhoto(camera);if(!item){status.textContent='No photo for this section. Capture or select a close-up first.';return}status.textContent='Running HSV plant-image screening…';try{const img=await load(item.url),scale=Math.min(1,720/Math.max(img.width,img.height)),w=Math.max(1,Math.round(img.width*scale)),h=Math.max(1,Math.round(img.height*scale)),canvas=document.createElement('canvas');canvas.width=w;canvas.height=h;const ctx=canvas.getContext('2d',{willReadFrequently:true});ctx.drawImage(img,0,0,w,h);const pixels=ctx.getImageData(0,0,w,h).data,mask=new Uint8Array(w*h),kind=new Uint8Array(w*h),color=window.GrowPlotHSV.analyzeRgba(pixels);let green=color.greenPixels,yellow=color.yellowPixels,brown=color.brownPixels,vegetation=green+yellow+brown;for(let y=0;y<h;y++)for(let x=0;x<w;x++){const n=y*w+x,k=n*4,category=window.GrowPlotHSV.classifyRgb(pixels[k],pixels[k+1],pixels[k+2]);if(category){mask[n]=1;kind[n]=category}}const seen=new Uint8Array(w*h),queue=new Int32Array(w*h),components=[],minArea=Math.max(100,Math.round(w*h*.002));for(let n=0;n<mask.length;n++){if(!mask[n]||seen[n])continue;let head=0,tail=1,area=0,xmin=w,ymin=h,xmax=0,ymax=0,rg=0,ry=0,rb=0;queue[0]=n;seen[n]=1;while(head<tail){const p=queue[head++],x=p%w,y=(p/w)|0;area++;if(kind[p]===1)rg++;else if(kind[p]===2)ry++;else rb++;xmin=Math.min(xmin,x);xmax=Math.max(xmax,x);ymin=Math.min(ymin,y);ymax=Math.max(ymax,y);const nb=[x>0?p-1:-1,x<w-1?p+1:-1,y>0?p-w:-1,y<h-1?p+w:-1];for(const q of nb)if(q>=0&&mask[q]&&!seen[q]){seen[q]=1;queue[tail++]=q}}if(area>=minArea)components.push({area,xmin,ymin,xmax,ymax,rg,ry,rb})}components.sort((a,b)=>b.area-a.area);const regions=components.slice(0,12);const originalCanvas=document.createElement('canvas');originalCanvas.width=w;originalCanvas.height=h;originalCanvas.getContext('2d').drawImage(canvas,0,0);ctx.lineWidth=Math.max(2,Math.round(w/300));ctx.font='bold '+Math.max(14,Math.round(w/35))+'px sans-serif';regions.forEach((r,i)=>{ctx.strokeStyle='#f9e55c';ctx.strokeRect(r.xmin,r.ymin,r.xmax-r.xmin+1,r.ymax-r.ymin+1);ctx.fillStyle='#103f2b';ctx.fillRect(r.xmin,r.ymin,28,25);ctx.fillStyle='#fff';ctx.fillText(String(i+1),r.xmin+7,r.ymin+19)});const percent=n=>Math.round(n*100/(w*h)),report={plotId:String(window.selectedPlotId||''),camera,at:new Date().toISOString(),source:item.fileName||'',candidateRegions:regions.length,greenPercent:percent(green),yellowPercent:percent(yellow),brownPercent:percent(brown),otherPercent:color.otherPercent,method:'HSV threshold segmentation and connected-region screen',colorSpace:'HSV',limitations:'HSV colour regions are not individual leaves. Background, overlapping leaves, lighting, shadows, and camera white balance can affect results. No YOLO model is packaged in this build.'};try{localStorage.setItem('growplotLeafCv:'+report.plotId+':cctv'+camera,JSON.stringify(report))}catch(e){}const png=canvas.toDataURL('image/jpeg',.83);
 const escapeHtml=v=>String(v).replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
 const leafCards=regions.map((r,i)=>{const total=r.rg+r.ry+r.rb||1, gp=Math.round(r.rg*100/total),yp=Math.round(r.ry*100/total),bp=Math.round(r.rb*100/total),pad=10,x=Math.max(0,r.xmin-pad),y=Math.max(0,r.ymin-pad),cw=Math.min(w-x,r.xmax-r.xmin+1+pad*2),ch=Math.min(h-y,r.ymax-r.ymin+1+pad*2),crop=document.createElement('canvas');crop.width=Math.max(1,cw);crop.height=Math.max(1,ch);crop.getContext('2d').drawImage(originalCanvas,x,y,cw,ch,0,0,cw,ch);const status=bp>=20?'Brown-like tissue prominent':yp>=25?'Yellow-like tissue prominent':gp>=70?'Predominantly green':'Mixed colour signals';const advice=bp>=20?'Inspect for necrosis, dryness or shadows.':yp>=25?'Inspect for chlorosis, light effects or natural ageing.':'Inspect surface, edges and underside for damage.';return '<article class="cv-leaf-card"><img alt="Candidate foliage region '+(i+1)+'" src="'+crop.toDataURL('image/jpeg',.7)+'"><p><b>Region '+(i+1)+'</b> · '+gp+'% green · '+yp+'% yellow · '+bp+'% brown</p><p class="'+(bp>=20||yp>=25?'cv-flag':'cv-ok')+'">'+status+'</p><p>'+advice+'</p></article>'}).join('');
 result.innerHTML='<img class="cv-preview" alt="Photo annotated with numbered candidate foliage regions" src="'+png+'"><div class="cv-metrics"><span><b>'+report.greenPercent+'%</b>Green</span><span><b>'+report.yellowPercent+'%</b>Yellow</span><span><b>'+report.brownPercent+'%</b>Brown</span><span><b>'+report.otherPercent+'%</b>Other</span></div><p><b>'+regions.length+' candidate foliage regions highlighted.</b> These numbered boxes are not a reliable leaf count: overlapping leaves may merge and single leaves may split.</p><p><b>Observations:</b> '+(report.yellowPercent>20?'Yellow-like pixels are prominent. ':'')+(report.brownPercent>12?'Brown-like pixels warrant closer inspection for dry edges or spots. ':'')+(report.yellowPercent<=20&&report.brownPercent<=12?'The screen found no dominant yellow/brown cue. ':'')+'Inspect leaf surfaces, margins and new growth before diagnosing.</p><p><b>Growth stage / age:</b> Cannot be determined reliably from colour screening alone; use species, planting date and whole-plant photographs.</p><p><b>Flowering / fruiting:</b> Not assessed by this colour-only detector.</p><p><b>Follow-up:</b> Photograph individual leaves against a plain background, including upper and lower surfaces. Check irrigation, root-zone moisture and growing-medium pH/EC before treatment.</p><small class="cv-caution">HSV colour screening only; YOLO model unavailable in this build. Preliminary visual screening only. No species identification, leaf-instance segmentation or disease confirmation is performed. Source image remains unchanged.</small>';status.textContent='Annotated image and per-region explanations complete · summary saved locally for this plot and section.'}catch(e){status.textContent='Unable to analyse photo: '+e.message}}

 // Explicit API: invoked by runDiagnosisScreen, not by a second click listener.

 // Vision API enhancement. Never store an API key in the Android app.
 const localAnalyze=analyze;
 const esc=v=>String(v==null?'':v).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 function boxCoords(box,w,h){
   if(!Array.isArray(box)||box.length!==4||box.some(v=>typeof v!=='number'||!Number.isFinite(v)))return null;
   const a=box.map((v,i)=>Math.max(0,Math.min(i%2===0?w:h,Math.round(v*(i%2===0?w:h)/1000))));
   return [Math.min(a[0],a[2]),Math.min(a[1],a[3]),Math.max(a[0],a[2]),Math.max(a[1],a[3])];
 }
 async function visionAnalyze(camera){
   const root=panel(camera);if(!root)return;
   const status=root.querySelector('#cvProgress'),result=root.querySelector('#cvResult');
   const endpoint=(localStorage.getItem('growplotVisionEndpoint')||'').trim();
   if(!endpoint){
     status.textContent='OpenAI vision backend not configured. Local screening shown below.';
     await localAnalyze(camera);
     const r=document.getElementById('growplotInlineResults')||root;
     r.insertAdjacentHTML('afterbegin','<p class="cv-caution">AI vision is not active. Configure its HTTPS backend below.</p>');
     showEndpointEditor(r);
     return;
   }
   if(!/^https:\/\//i.test(endpoint)){
     status.textContent='Vision endpoint must use HTTPS.';
     showEndpointEditor(root);return;
   }
   const item=findPhoto(camera);
   if(!item){status.textContent='No photograph available for AI analysis.';showEndpointEditor(root);return;}
   status.textContent='Analysing plant, pot and individual leaves using OpenAI vision…';
   try{
     const img=await load(item.url),scale=Math.min(1,1024/Math.max(img.width,img.height));
     const w=Math.round(img.width*scale),h=Math.round(img.height*scale);
     const canvas=document.createElement('canvas');canvas.width=w;canvas.height=h;
     const ctx=canvas.getContext('2d');ctx.drawImage(img,0,0,w,h);
     const controller=new AbortController(),timeout=setTimeout(()=>controller.abort(),80000);
     let response;
     try{
       response=await fetch(endpoint.replace(/\/$/,'')+'/api/plant-analysis',{
         method:'POST',headers:{'Content-Type':'application/json'},
         body:JSON.stringify({image:canvas.toDataURL('image/jpeg',0.82)}),
         signal:controller.signal
       });
     }finally{clearTimeout(timeout)}
     const data=await response.json();
     if(!response.ok||!data.analysis)throw Error(data.error||'Vision server returned no analysis');
     const a=data.analysis;
     ctx.lineWidth=Math.max(2,Math.round(w/250));ctx.font='bold '+Math.max(13,Math.round(w/35))+'px sans-serif';
     function outline(box,color,label,ellipse){
       const b=boxCoords(box,w,h);if(!b)return;
       ctx.strokeStyle=color;ctx.fillStyle=color;ctx.beginPath();
       if(ellipse)ctx.ellipse((b[0]+b[2])/2,(b[1]+b[3])/2,Math.max(5,(b[2]-b[0])/2),Math.max(5,(b[3]-b[1])/2),0,0,Math.PI*2);
       else ctx.rect(b[0],b[1],b[2]-b[0],b[3]-b[1]);
       ctx.stroke();ctx.fillRect(b[0],Math.max(0,b[1]-22),Math.max(28,label.length*10),22);
       ctx.fillStyle='#ffffff';ctx.fillText(label,b[0]+4,Math.max(17,b[1]-5));
     }
     outline(a.plant_box,'#32cf69','PLANT',false);
     outline(a.container_box,'#34b9ff','POT',false);
     const leaves=Array.isArray(a.leaves)?a.leaves.slice(0,20):[];
     leaves.forEach((leaf,i)=>outline(leaf.box,'#ffe35a','L'+(i+1),true));
     const items=v=>Array.isArray(v)?v.slice(0,12).map(x=>'<li>'+esc(x)+'</li>').join(''):'';
     result.innerHTML='<img class="cv-preview" alt="Original photograph with AI-estimated plant, pot and leaf outlines" src="'+canvas.toDataURL('image/jpeg',.87)+'">'+
       '<p><b>Plant:</b> '+esc(a.plant_name||'Uncertain')+' · confidence: '+esc(a.plant_confidence||'unknown')+'</p>'+
       '<p><b>Container:</b> '+(a.container_box?'Blue boundary':'Not confidently located')+' · <b>Leaf candidates:</b> '+leaves.length+'</p>'+
       '<p><b>Visible observations</b></p><ul>'+items(a.observations)+'</ul>'+
       '<p><b>Recommended checks</b></p><ul>'+items(a.recommendations)+'</ul>'+
       '<p><b>Individual leaves</b></p><ul>'+leaves.map((l,i)=>'<li>L'+(i+1)+': '+esc(l.condition||'Not assessed')+' ('+esc(l.confidence||'unknown')+')</li>').join('')+'</ul>'+
       '<small class="cv-caution">AI-estimated outlines are approximate, not validated segmentation. '+esc(a.limitations||'Visual screening cannot confirm disease.')+'</small>';
     status.textContent='HSV screening and optional vision analysis completed · '+esc(data.model||'vision model');
     try{localStorage.setItem('growplotVisionResult:'+String(window.selectedPlotId||'')+':'+camera,JSON.stringify({at:new Date().toISOString(),analysis:a,model:data.model}))}catch(e){}
     showEndpointEditor(root);
   }catch(error){
     status.textContent='AI analysis unavailable: '+error.message+'. Local colour screening is available.';
     showEndpointEditor(root);
   }
 }
 function showEndpointEditor(root){
   if(root.querySelector('#growplotVisionSettings'))return;
   const wrap=document.createElement('details');wrap.id='growplotVisionSettings';
   wrap.innerHTML='<summary>OpenAI vision backend settings</summary><label>HTTPS backend URL (no API key): <input id="growplotVisionUrl" type="url" style="max-width:100%;width:95%" placeholder="https://your-backend.example"></label><button type="button" id="growplotVisionSave">Save URL</button>';
   root.appendChild(wrap);
   wrap.querySelector('input').value=localStorage.getItem('growplotVisionEndpoint')||'';
   wrap.querySelector('button').addEventListener('click',()=>{
     const url=wrap.querySelector('input').value.trim().replace(/\/$/,'');
     if(url&&!/^https:\/\//i.test(url)){alert('HTTPS URL required');return;}
     localStorage.setItem('growplotVisionEndpoint',url);
     alert('Backend URL saved. Run diagnosis again.');
   });
 }

 async function runYolo(camera){
   const root=panel(camera),result=root&&root.querySelector('#cvResult'),progress=root&&root.querySelector('#cvProgress');
   if(!root||!result)return;
   const bridge=window.GrowPlotBridge,config=(()=>{try{return JSON.parse(bridge?.getDiagnosisApiConfig()||'{}')}catch(_){return {}}})();
   const base=String(config.url||'').trim(),token=String(config.proxyToken||'').trim();
   const esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
   const section=document.createElement('section');section.className='diagnosis-analysis provider-analysis';section.id='growplotYoloResults';
   result.appendChild(section);
   if(!base||!token){section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>The diagnosis service URL and app token are not configured in this build. Local HSV screening and the existing provider checks will continue.</p>';return}
   let endpoint;
   try{const u=new URL(base);if(u.protocol!=='https:')throw Error();u.pathname=u.pathname.replace(/\/$/,'').replace(/\/v1\/diagnose$/,'')+'/v1/yolo';u.search='';endpoint=u.toString()}catch(_){section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>The configured diagnosis service URL is invalid; HSV screening and provider checks will continue.</p>';return}
   let media=[];try{media=(typeof diagnosisMediaFor==='function'?diagnosisMediaFor(camera):[]).filter(x=>x.kind==='photo'&&x.url).slice(0,5)}catch(_){}
   if(!media.length){section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>No diagnosis photos are available.</p>';return}
   try{
     if(progress)progress.textContent='Running YOLO-World plant-part detection…';
     const form=new FormData();
     for(let i=0;i<media.length;i++){
       const im=await load(media[i].url),scale=Math.min(1,1280/Math.max(im.naturalWidth,im.naturalHeight)),canvas=document.createElement('canvas');
       canvas.width=Math.max(1,Math.round(im.naturalWidth*scale));canvas.height=Math.max(1,Math.round(im.naturalHeight*scale));
       canvas.getContext('2d').drawImage(im,0,0,canvas.width,canvas.height);
       const blob=await new Promise(resolve=>canvas.toBlob(resolve,'image/jpeg',0.82));
       if(blob)form.append('photos',blob,media[i].fileName||('diagnosis-'+(i+1)+'.jpg'));
     }
     const controller=new AbortController(),timeout=setTimeout(()=>controller.abort(),130000);let response;
     try{response=await fetch(endpoint,{method:'POST',headers:{Authorization:'Bearer '+token},body:form,signal:controller.signal})}finally{clearTimeout(timeout)}
     const data=await response.json().catch(()=>({}));if(!response.ok||data.status!=='ok')throw Error(data.message||data.detail||'YOLO inference is unavailable.');
     const images=Array.isArray(data.images)?data.images:[],summaries=[];
     for(const row of images){
       const item=media[Number(row.imageIndex)||0];if(!item)continue;const im=await load(item.url),canvas=document.createElement('canvas'),scale=Math.min(1,1024/Math.max(im.naturalWidth,im.naturalHeight));
       canvas.width=Math.max(1,Math.round(im.naturalWidth*scale));canvas.height=Math.max(1,Math.round(im.naturalHeight*scale));const ctx=canvas.getContext('2d');ctx.drawImage(im,0,0,canvas.width,canvas.height);
       const detections=Array.isArray(row.detections)?row.detections:[];ctx.lineWidth=Math.max(2,Math.round(canvas.width/300));ctx.font='bold '+Math.max(13,Math.round(canvas.width/38))+'px sans-serif';
       detections.forEach(d=>{if(!Array.isArray(d.box)||d.box.length!==4)return;const b=d.box.map(v=>Number(v)/1000),x1=Math.max(0,b[0]*canvas.width),y1=Math.max(0,b[1]*canvas.height),x2=Math.min(canvas.width,b[2]*canvas.width),y2=Math.min(canvas.height,b[3]*canvas.height),label=String(d.label||'plant part'),score=Number(d.confidence);ctx.strokeStyle='#14a66a';ctx.fillStyle='#14a66a';ctx.strokeRect(x1,y1,x2-x1,y2-y1);const tag=label+(Number.isFinite(score)?' '+Math.round(score*100)+'%':'');ctx.fillRect(x1,Math.max(0,y1-23),Math.min(canvas.width-x1,tag.length*8+10),22);ctx.fillStyle='#fff';ctx.fillText(tag,x1+5,Math.max(16,y1-6))});
       const countByLabel={};detections.forEach(d=>countByLabel[d.label]=(countByLabel[d.label]||0)+1);summaries.push('<div><img class="cv-preview" alt="YOLO-World detected plant parts" src="'+canvas.toDataURL('image/jpeg',0.86)+'"><p><b>Photo '+(Number(row.imageIndex)+1)+':</b> '+(detections.length?detections.map(d=>esc(d.label)+' '+Math.round((Number(d.confidence)||0)*100)+'%').join(' · '):'No plant-part regions detected.')+'</p></div>')
     }
     section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>Model: '+esc(data.model||'YOLO-World')+' · '+esc(data.task||'object detection')+'</p>'+(summaries.length?summaries.join(''):'<p>No detection results were returned.</p>')+'<p class="cv-caution">'+esc(data.limitations||'This detector locates broad plant parts; it does not identify species or diagnose disease.')+'</p>';
     try{localStorage.setItem('growplotYolo:'+String(window.selectedPlotId||'')+':cctv'+camera,JSON.stringify({at:new Date().toISOString(),model:data.model,images:data.images,limitations:data.limitations}))}catch(_){}
     if(progress)progress.textContent='HSV screen and YOLO-World plant-part detection completed.';
   }catch(error){section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>YOLO inference could not run: '+esc(error.message||error)+'</p><p class="cv-caution">HSV screening and species/disease provider checks remain available. Configure and deploy the YOLO service to enable this step.</p>';if(progress)progress.textContent='YOLO unavailable; continuing with the other diagnosis checks.'}
 }

 function openPlantNetSettings(){if(typeof window.closeDiagnosis==='function')window.closeDiagnosis();if(typeof window.showPanel==='function')window.showPanel('settings');setTimeout(()=>{const section=document.getElementById('pluginApiSettingsSection'),field=document.getElementById('plantnetApiKey');if(section){section.open=true;section.scrollIntoView({behavior:'smooth',block:'center'})}if(field)field.focus()},100)}
 function plantSpeciesMarkup(report,camera,plotId){const id=report&&report.speciesIdentification;if(!id)return '';const rows=Array.isArray(id.results)?id.results.slice(0,5):[],esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));let body='';if(id.status==='ok'&&rows.length){body='<ol>'+rows.map((row,index)=>{const sp=row.species||{},latin=sp.scientificNameWithoutAuthor||sp.scientificName||row.name||'Unlabelled species',common=Array.isArray(sp.commonNames)?sp.commonNames.slice(0,2).join(', '):'',score=Number(row.score),label=(common?esc(common)+' · ':'')+esc(latin),confidence=Number.isFinite(score)?Math.round(score*100)+'%':'';return '<li><b>'+label+'</b>'+(confidence?' · '+confidence:'')+' <button type="button" class="diagnose-secondary" onclick="window.GrowPlotCV.confirmSpecies('+Number(camera)+',decodeURIComponent(\''+encodeURIComponent(plotId||'').replace(/'/g,'%27')+'\'),'+index+')">Use this species</button></li>'}).join('')+'</ol><p>Ranked from '+(Number(id.imageCountSubmitted)||rows.length)+' submitted image(s). These are image-based candidates, not confirmed identifications. Use close, well-lit views of the same plant for the strongest comparison.</p>'}else if(id.status==='not_configured'){body='<p>Image-based species recognition is off because no Pl@ntNet API key is configured on this device.</p><button type="button" class="diagnose-secondary" onclick="window.GrowPlotCV.openPlantNetSettings()">Set up species recognition</button>'}else if(id.status==='error'){body='<p>'+esc(id.message||'Pl@ntNet species identification was unavailable.')+'</p><button type="button" class="diagnose-secondary" onclick="window.GrowPlotCV.openPlantNetSettings()">Check species recognition settings</button>'}else{body='<p>No ranked species candidates were returned. Capture clear leaf, flower or fruit images and retry.</p>'}return '<section class="diagnosis-analysis provider-analysis cv-species-results"><h4>Plant species · Pl@ntNet image identification</h4>'+body+(Number.isFinite(Number(id.remainingIdentificationRequests))?'<small>Species-identification requests remaining today: '+esc(id.remainingIdentificationRequests)+'</small>':'')+'</section>'}
 function installSpeciesResults(){const markup=window.diagnosisProviderMarkup;if(typeof markup==='function'&&!markup.__growplotSpeciesWrapper){const wrapped=function(report,camera,plotId){return plantSpeciesMarkup(report,Number(camera)||0,String(plotId||''))+markup.call(this,report,camera,plotId)};wrapped.__growplotSpeciesWrapper=true;window.diagnosisProviderMarkup=wrapped}const original=window.onPlantNetDiagnosis;if(typeof original==='function'&&!original.__growplotSpeciesWrapper){const wrapped=function(camera,plotId,json){let report={};try{report=JSON.parse(json||'{}')}catch(e){}if(report.speciesIdentification){try{localStorage.setItem('growplotSpeciesCandidates:'+plotId+':cctv'+camera,JSON.stringify(report.speciesIdentification))}catch(e){}}return original.apply(this,arguments)};wrapped.__growplotSpeciesWrapper=true;window.onPlantNetDiagnosis=wrapped}}
 function confirmSpecies(camera,plotId,index){let identification;try{identification=JSON.parse(localStorage.getItem('growplotSpeciesCandidates:'+plotId+':cctv'+camera)||'null')}catch(e){}const row=identification&&Array.isArray(identification.results)?identification.results[index]:null;if(!row||!row.species)return;const species=row.species,scientific=species.scientificNameWithoutAuthor||species.scientificName||row.name||'',name=(Array.isArray(species.commonNames)&&species.commonNames[0])||scientific,score=Number(row.score),choice={name,scientific,confirmed:true,identificationSource:'Pl@ntNet image identification',confidence:Number.isFinite(score)?score:null};try{localStorage.setItem('growplotSpecies:'+plotId+':cctv'+camera,JSON.stringify(choice));let analysis=JSON.parse(localStorage.getItem('growplotAnalysis:'+plotId+':cctv'+camera)||'null');if(analysis){analysis.speciesContext=scientific;analysis.speciesIdentification=identification;analysis.speciesConfirmedAt=new Date().toISOString();const symptom=localStorage.getItem('growplotSymptom:'+plotId+':cctv'+camera)||'';if(typeof window.diagnosisDiseaseCandidates==='function')analysis.diseaseCandidates=window.diagnosisDiseaseCandidates(camera,symptom,choice);localStorage.setItem('growplotAnalysis:'+plotId+':cctv'+camera,JSON.stringify(analysis))}}catch(e){}if(typeof window.openDiagnosis==='function')window.openDiagnosis(camera)}
 installSpeciesResults();
 window.GrowPlotCV={analyze:localAnalyze,localAnalyze:localAnalyze,runYolo,analyzeWithVision:visionAnalyze,confirmSpecies,openPlantNetSettings};
})();
