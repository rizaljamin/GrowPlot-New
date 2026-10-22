/* GrowPlot local CV screening, Build 06 candidate. No remote services or invented species diagnoses. */
(function(){'use strict';
 const css=`.cv-panel{margin:12px 0;padding:12px;border:1px solid #c8dfd0;border-radius:12px;background:#f6fbf7;color:#193e2b}.cv-panel h4{margin:0 0 7px;font-size:15px}.cv-panel p{font-size:12px;line-height:1.5;margin:6px 0}.cv-panel button{padding:9px 12px;border:0;border-radius:8px;background:#17553a;color:white;font-weight:700}.cv-preview{width:100%;max-height:400px;object-fit:contain;background:#162d20;border-radius:8px}.cv-metrics{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:5px;margin:8px 0}.cv-metrics span{padding:8px 4px;text-align:center;background:white;border:1px solid #d8e8dc;border-radius:7px;font-size:11px}.cv-metrics b{display:block;font-size:15px}.cv-caution{color:#8a5b17}.cv-thumbnails{display:flex;gap:6px;overflow-x:auto}.cv-leaf-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;margin:10px 0}.cv-leaf-card{background:white;border:1px solid #d8e8dc;border-radius:9px;padding:7px}.cv-leaf-card img{width:100%;height:100px;object-fit:contain;background:#edf3ee;border-radius:5px}.cv-leaf-card p{font-size:11px;margin:4px 0}.cv-flag{font-weight:700;color:#8b5918}.cv-ok{font-weight:700;color:#1e6a42}.cv-note{font-size:11px;color:#526659}.cv-panel small{display:block;line-height:1.45}`;
 document.head.insertAdjacentHTML('beforeend','<style>'+css+'</style>');
 function currentPanel(){return document.querySelector('#diagnosisModal .diagnosis-dialog')}
 function panel(camera){
  const dedicated=document.getElementById('growplotCVWindow');
  if(dedicated&&dedicated.style.display!=='none'){
    const output=dedicated.querySelector('#growplotCVOutput');
    if(output)return output;
  }
  const inline=document.getElementById('growplotInlineResults');
  if(inline){
    inline.innerHTML='<p id="cvProgress" role="status">Preparing foliage analysis…</p><div id="cvResult"></div>';
    return inline;
  }
  let host=currentPanel();if(!host)return null;let el=document.getElementById('cvLeafPanel');if(!el){el=document.createElement('section');el.className='cv-panel';el.id='cvLeafPanel';const target=host.querySelector('#diagnosisDescription');if(target)target.insertAdjacentElement('beforebegin',el);else host.appendChild(el)}el.innerHTML='<h4>Plant image analysis · Section '+String(camera).padStart(2,'0')+'</h4><p>Run Diagnosis screens HSV colour cues on this device and attempts YOLO-World detection of broad plant parts through the diagnosis service. Plant-part boxes are not species identification or disease confirmation.</p><p id="cvProgress" role="status"></p><div id="cvResult"></div>';return el}
 function feedPhoto(camera){const card=typeof cameraRoot!=='undefined'&&cameraRoot.children?cameraRoot.children[Number(camera)-1]:null,url=card?.dataset?.latestPhotoUrl||('https://appassets.androidplatform.net/assets/images/'+encodeURIComponent(String(window.selectedPlotId||''))+'/cctv_'+String(camera).padStart(2,'0')+'.jpg?refresh='+Date.now());return {url,kind:'photo',fileName:'Latest Image_Feed snapshot.jpg',feedSnapshot:true}}
 function findPhoto(camera){try{const photos=(typeof diagnosisMediaFor==='function'?diagnosisMediaFor(camera):[]).filter(x=>x.kind==='photo'&&x.url);let latest=photos.slice(-1)[0]||null,best=-1;photos.forEach(item=>{const time=Date.parse(item.capturedAt||item.timestamp||item.date||'');if(Number.isFinite(time)&&time>best){latest=item;best=time}});return latest||feedPhoto(camera)}catch(e){return feedPhoto(camera)}}
 function installDiagnosisSubsections(){
  const style='.diagnosis-results-grid{display:grid;gap:10px;margin-top:10px}.diagnosis-result-subsection{min-width:0;padding:11px;border:1px solid #cfe0d2;border-radius:10px;background:#f8fbf8}.diagnosis-result-subsection>h4{margin:0 0 8px;color:#174a35;font-size:14px}.diagnosis-result-subsection .diagnosis-analysis{margin:6px 0}.diagnosis-result-subsection .diagnosis-analysis h4{display:none}.cv-hsv-card{margin:8px 0;padding:8px;border:1px solid #d8e8dc;border-radius:8px;background:#fff}.cv-hsv-card>b{display:block;margin-bottom:6px;color:#355243;font-size:11px}.cv-hsv-card .cv-preview{max-height:280px}.cv-hsv-total{margin-top:10px;padding:8px;border-radius:8px;background:#fff}.cv-hsv-total> b{font-size:11px;color:#174a35}.cv-hsv-card .cv-metrics,.cv-hsv-total .cv-metrics{grid-template-columns:repeat(2,minmax(0,1fr))}.cv-hsv-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:7px;margin:8px 0}.cv-hsv-channel{margin:0;padding:6px;border:1px solid #d8e8dc;border-radius:7px;background:#fff}.cv-hsv-channel b{display:block;font-size:10px;color:#355243;margin-bottom:4px}.cv-hsv-channel img{display:block;width:100%;max-height:180px;object-fit:contain;background:#17231d;border-radius:4px}.cv-pixel-metrics{grid-template-columns:repeat(2,minmax(0,1fr))}.cv-pixel-count{display:block;font-size:10px;color:#51665a;margin-top:2px}.diagnosis-result-status{font-size:10px;color:#617166;line-height:1.4}';
  if(!document.getElementById('growplotDiagnosisSubsectionStyle')){const s=document.createElement('style');s.id='growplotDiagnosisSubsectionStyle';s.textContent=style;document.head.appendChild(s)}
  const modal=document.getElementById('diagnosisModal'),analysis=modal&&modal.querySelector('#diagnosisAnalysis');if(!analysis)return;
  if(analysis.dataset.subsectionsReady==='1')return;
  const description=analysis.querySelector('#diagnosisDescription'),vertex=analysis.querySelector('#diagnosisVertexAnalysis'),providers=analysis.querySelector('#diagnosisProviderAnalysis'),inline=analysis.querySelector('#growplotInlineCV');
  if(vertex&&/GROWPLOT_VERTEX_API_KEY|Vertex API key is not included/i.test(vertex.textContent||'')){try{localStorage.removeItem(diagnosisVertexKey(camera))}catch(e){}vertex.innerHTML=diagnosisVertexMarkup({status:'pending',message:'The previous API-key warning is obsolete. Run diagnosis to request Vertex directly with Google Cloud authorization.'})}
  const section=document.createElement('div');section.className='diagnosis-results-grid';section.id='diagnosisResultsGrid';
  const make=(title,id)=>{const box=document.createElement('section');box.className='diagnosis-result-subsection';box.setAttribute('aria-live','polite');const h=document.createElement('h4');h.textContent=title;box.appendChild(h);const body=document.createElement('div');body.id=id;box.appendChild(body);return {box,body}};
  const cv=make('OpenCV analysis','opencvAnalysisOutput'),vx=make('Vertex Analysis','vertexAnalysisOutput'),pk=make('Pl@ntNet / Kindwise Analysis','plantNetKindwiseOutput');
  if(inline){inline.style.display='block';inline.style.margin='0';inline.style.padding='0';const original=inline.querySelector('section');if(original)original.remove();const cvSection=inline.querySelector('section');if(cvSection){const heading=cvSection.querySelector('h4');if(heading)heading.textContent='HSV image and colour-pixel analysis';cvSection.style.border='0';cvSection.style.padding='0';cvSection.style.background='transparent';cv.body.appendChild(inline)}}
  if(description){description.hidden=true;description.setAttribute('aria-hidden','true');cv.body.appendChild(description)}
  if(vertex){vertex.id='diagnosisVertexAnalysis';vx.body.appendChild(vertex)}
  if(providers){providers.id='diagnosisProviderAnalysis';pk.body.appendChild(providers)}
  section.append(cv.box,vx.box,pk.box);analysis.replaceWith(section);section.dataset.subsectionsReady='1';
  const step=section.closest('.diagnosis-step');if(step){const heading=step.querySelector('.diagnosis-section-heading h4');if(heading)heading.textContent='3. Diagnosis results';const intro=step.querySelector('.diagnosis-section-heading')?.nextElementSibling;if(intro&&intro.tagName==='P')intro.textContent='The results are separated by method. OpenCV displays the HSV colour mask and yellow, green, and brown pixel percentages.';step.querySelectorAll('.diagnosis-analysis-note').forEach(note=>{if(note.textContent.includes('Photo screening'))note.textContent='Run diagnosis to populate the three result panels below.'})}
  if(window.GrowPlotCV)window.GrowPlotCV.runYolo=async()=>({status:'not_requested',message:'This OpenCV subsection is limited to HSV colour output.'});
 }
 const originalRender=window.renderDiagnosisModal;
 if(typeof originalRender==='function'&&!originalRender.__growplotSubsections){const wrapped=function(camera){const result=originalRender.apply(this,arguments);installDiagnosisSubsections();return result};wrapped.__growplotSubsections=true;window.renderDiagnosisModal=wrapped}

 function load(url){return new Promise((resolve,reject)=>{const im=new Image();im.onload=()=>resolve(im);im.onerror=()=>reject(Error('Image unavailable. Select or capture a local close-up.'));im.src=url})}
 function ensureHSV(){if(window.GrowPlotHSV)return Promise.resolve(window.GrowPlotHSV);if(window.__growPlotHSVLoading)return window.__growPlotHSVLoading;window.__growPlotHSVLoading=new Promise((resolve,reject)=>{const script=document.createElement('script');script.src='plant-hsv.js';script.onload=()=>window.GrowPlotHSV?resolve(window.GrowPlotHSV):reject(Error('plant-hsv.js loaded without initializing GrowPlotHSV'));script.onerror=()=>reject(Error('Unable to load local HSV analysis'));document.head.appendChild(script)}).catch(error=>{window.__growPlotHSVLoading=null;throw error});return window.__growPlotHSVLoading}
 function simplifyDiagnosisUi(){const modal=document.getElementById('diagnosisModal');if(!modal)return;modal.querySelector('#openCVAnalysisButton')?.remove();const identity=modal.querySelector('.species-confirm'),identityStatus=identity&&identity.querySelector('p');if(identityStatus&&identityStatus.textContent.includes('Visual estimate')){identityStatus.textContent='Preset estimate for this CCTV section only; it is not based on this photo. Run Diagnosis to get Pl@ntNet image-based species matches.';identity.querySelector('button[onclick^="confirmDiagnosisEstimate"]')?.remove()}modal.querySelectorAll('.diagnosis-analysis-note').forEach(note=>{if(note.textContent.includes('Photo screening')||note.textContent.includes('colour'))note.textContent='Run Diagnosis performs on-device HSV colour screening, attempts YOLO-World plant-part detection through the configured service, and asks Pl@ntNet to identify species and check disease candidates. YOLO locates broad plant parts; it does not diagnose disease.'});const estimate=modal.querySelector('.species-confirm');if(estimate){const status=estimate.querySelector('p');if(status&&status.textContent.includes('Visual estimate'))status.innerHTML='This is a preset visual estimate for the CCTV section, not an image-based species identification. Run Diagnosis to compare the submitted photos with Pl@ntNet.';estimate.querySelector('button[onclick^="confirmDiagnosisEstimate"]')?.remove()}const headings=modal.querySelectorAll('h4');headings.forEach(h=>{if(h.textContent.trim()==='2. CV diagnosis')h.textContent='2. Plant image analysis'})}
 async function screenUrl(url){const im=await load(url),side=112,scale=Math.min(side/im.naturalWidth,side/im.naturalHeight),w=Math.max(1,Math.round(im.naturalWidth*scale)),h=Math.max(1,Math.round(im.naturalHeight*scale)),canvas=document.createElement('canvas');canvas.width=w;canvas.height=h;const ctx=canvas.getContext('2d',{willReadFrequently:true});if(!ctx)throw Error('Canvas is unavailable');ctx.drawImage(im,0,0,w,h);return Object.assign({},window.GrowPlotHSV.analyzeRgba(ctx.getImageData(0,0,w,h).data),{colorSpace:'HSV',width:im.naturalWidth,height:im.naturalHeight})}
 window.screenDiagnosisImage=(url)=>ensureHSV().then(()=>screenUrl(url));
 async function channelImage(values,width,height){
  const canvas=document.createElement('canvas');canvas.width=width;canvas.height=height;
  const ctx=canvas.getContext('2d');if(!ctx)throw Error('HSV channel rendering is unavailable');
  const image=ctx.createImageData(width,height),out=image.data;
  for(let i=0;i<values.length;i++){const p=i*4,v=values[i];out[p]=v;out[p+1]=v;out[p+2]=v;out[p+3]=255}
  ctx.putImageData(image,0,0);return canvas.toDataURL('image/png');
 }
 async function analyze(camera){
 simplifyDiagnosisUi();const root=panel(camera);if(!root)return;
 const status=root.querySelector('#cvProgress'),result=root.querySelector('#cvResult');
 if(!result){if(status)status.textContent='OpenCV output area is missing from Section 1.';return}
 result.innerHTML='';const item=findPhoto(camera),esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 if(!item||!item.url){const m='Photo reading failed: no saved photo or latest Image_Feed snapshot is available for this CCTV section.';if(status)status.textContent=m;result.innerHTML='<p class="cv-caution" role="status">'+esc(m)+'</p>';return {ok:false,error:m}}
 try{
  if(status)status.textContent='Reading photo: '+(item.fileName||'latest Image_Feed snapshot')+'…';
  const im=await new Promise((resolve,reject)=>{const image=new Image();image.onload=()=>resolve(image);image.onerror=()=>reject(Error('The image file could not be opened. It may be unavailable, incomplete, or unsupported.'));image.src=item.url});
  const scale=Math.min(1,480/Math.max(im.naturalWidth,im.naturalHeight)),w=Math.max(1,Math.round(im.naturalWidth*scale)),h=Math.max(1,Math.round(im.naturalHeight*scale)),canvas=document.createElement('canvas');canvas.width=w;canvas.height=h;
  const ctx=canvas.getContext('2d',{willReadFrequently:true});if(!ctx)throw Error('The browser could not create a canvas to read the photo.');ctx.drawImage(im,0,0,w,h);
  let pixels;try{pixels=canvas.toDataURL('image/png').split(',')[1]}catch(e){throw Error('The photo loaded, but browser security blocked pixel access: '+(e.message||e))}
  if(!pixels)throw Error('The photo loaded, but no pixel data could be read.');
  if(status)status.textContent='Photo reading succeeded: '+w+' × '+h+' pixels sampled. Starting OpenCV HSV analysis…';
  await new Promise(resolve=>requestAnimationFrame(()=>setTimeout(resolve,20)));
  if(!window.GrowPlotOpenCV||typeof window.GrowPlotOpenCV.analyzeImage!=='function')throw Error('OpenCV native bridge is missing from this APK. Install the latest GrowPlot build.');
  const report=JSON.parse(window.GrowPlotOpenCV.analyzeImage(pixels)||'{}');if(!report.ok)throw Error(report.error||'OpenCV returned no analysis result.');
  const src=k=>'data:image/png;base64,'+report[k],name=esc(item.fileName||'Latest Image_Feed snapshot');
  result.innerHTML='<p class="cv-note">Photo reading succeeded for <b>'+name+'</b>. OpenCV converted the image to HSV. The preview is converted back for display; grayscale images show H, S and V channels.</p>'+
  '<article class="cv-hsv-card"><b>HSV photo and channels · '+report.width+' × '+report.height+' sampled pixels</b><div class="cv-hsv-grid">'+
  '<figure class="cv-hsv-channel"><b>HSV-converted photo</b><img alt="Photo converted to HSV with OpenCV and back for display" src="'+src('preview')+'"></figure>'+
  '<figure class="cv-hsv-channel"><b>Hue (H)</b><img alt="OpenCV Hue channel" src="'+src('hue')+'"></figure>'+
  '<figure class="cv-hsv-channel"><b>Saturation (S)</b><img alt="OpenCV Saturation channel" src="'+src('saturation')+'"></figure>'+
  '<figure class="cv-hsv-channel"><b>Value (V)</b><img alt="OpenCV Value channel" src="'+src('value')+'"></figure>'+
  '<figure class="cv-hsv-channel"><b>Yellow, green and brown mask</b><img alt="OpenCV HSV colour mask" src="'+src('mask')+'"></figure>'+
  '<div class="cv-hsv-channel"><b>Colour-pixel analysis</b><div class="cv-metrics cv-pixel-metrics">'+
  '<span><b>'+report.yellowPercent+'%</b>Yellow<span class="cv-pixel-count">'+Number(report.yellowPixels).toLocaleString()+' pixels</span></span>'+
  '<span><b>'+report.greenPercent+'%</b>Green<span class="cv-pixel-count">'+Number(report.greenPixels).toLocaleString()+' pixels</span></span>'+
  '<span><b>'+report.brownPercent+'%</b>Brown<span class="cv-pixel-count">'+Number(report.brownPixels).toLocaleString()+' pixels</span></span>'+
  '<span><b>'+report.otherPercent+'%</b>Other<span class="cv-pixel-count">'+Number(report.otherPixels).toLocaleString()+' pixels</span></span></div><small>'+Number(report.totalPixels).toLocaleString()+' total pixels</small></div></div></article>'+
  '<small class="cv-caution">Counts include soil and background. These are colour measurements, not a plant-health score or disease diagnosis.</small>';
  if(status)status.textContent='OpenCV HSV analysis succeeded: '+Number(report.totalPixels).toLocaleString()+' pixels counted.';return report;
 }catch(e){const m=e&&e.message?e.message:String(e||'Unknown image-processing error');if(status)status.textContent='HSV analysis failed: '+m;result.innerHTML='<p class="cv-caution" role="status">OpenCV analysis failed: '+esc(m)+'</p>';return {ok:false,error:m}}
}
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
   if(!base||!token){const message='YOLO did not run because the diagnosis service URL and app token are not configured in this build.';section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>'+esc(message)+'</p><p>Local HSV and foliage CV results are still available. Deploy the YOLO service and configure the diagnosis proxy before expecting detections.</p>';return {status:'not_configured',message}}
   let endpoint;
   try{const u=new URL(base);if(u.protocol!=='https:')throw Error();u.pathname=u.pathname.replace(/\/$/,'').replace(/\/v1\/diagnose$/,'')+'/v1/yolo';u.search='';endpoint=u.toString()}catch(_){const message='YOLO service URL is invalid; inference did not run.';section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>'+esc(message)+'</p>';return {status:'error',message}}
   let media=[];try{media=(typeof diagnosisMediaFor==='function'?diagnosisMediaFor(camera):[]).filter(x=>x.kind==='photo'&&x.url).slice(0,5)}catch(_){}
   if(!media.length){const message='No diagnosis photos are available for YOLO inference.';section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>'+esc(message)+'</p>';return {status:'no_photos',message}}
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
     if(progress)progress.textContent='HSV screen and YOLO-World plant-part detection completed.';return Object.assign({status:'ok'},data);
   }catch(error){section.innerHTML='<h4>YOLO-World plant-part detection</h4><p>YOLO inference could not run: '+esc(error.message||error)+'</p><p class="cv-caution">HSV screening and species/disease provider checks remain available. Configure and deploy the YOLO service to enable this step.</p>';if(progress)progress.textContent='YOLO unavailable; continuing with the other diagnosis checks.';return {status:'error',message:error.message||'YOLO inference failed.'}}
 }

 function openPlantNetSettings(){if(typeof window.closeDiagnosis==='function')window.closeDiagnosis();if(typeof window.showPanel==='function')window.showPanel('settings');setTimeout(()=>{const section=document.getElementById('pluginApiSettingsSection'),field=document.getElementById('plantnetApiKey');if(section){section.open=true;section.scrollIntoView({behavior:'smooth',block:'center'})}if(field)field.focus()},100)}
 function plantSpeciesSelectionMarkup(report,camera,plotId){const id=report&&report.speciesIdentification;if(!id)return '';const rows=Array.isArray(id.results)?id.results.slice(0,5):[],esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));let body='';if(id.status==='ok'&&rows.length){body='<ol>'+rows.map((row,index)=>{const sp=row.species||{},latin=sp.scientificNameWithoutAuthor||sp.scientificName||row.name||'Unlabelled species',common=Array.isArray(sp.commonNames)?sp.commonNames.slice(0,2).join(', '):'',score=Number(row.score),label=(common?esc(common)+' · ':'')+esc(latin),confidence=Number.isFinite(score)?Math.round(score*100)+'%':'';return '<li><b>'+label+'</b>'+(confidence?' · '+confidence:'')+' <button type="button" class="diagnose-secondary" onclick="window.GrowPlotCV.confirmSpecies('+Number(camera)+',decodeURIComponent(\''+encodeURIComponent(plotId||'').replace(/'/g,'%27')+'\'),'+index+')">Use this species</button></li>'}).join('')+'</ol><p>Ranked from '+(Number(id.imageCountSubmitted)||rows.length)+' submitted image(s). These are image-based candidates, not confirmed identifications. Use close, well-lit views of the same plant for the strongest comparison.</p>'}else if(id.status==='not_configured'){body='<p>Image-based species recognition is off because no Pl@ntNet API key is configured on this device.</p><button type="button" class="diagnose-secondary" onclick="window.GrowPlotCV.openPlantNetSettings()">Set up species recognition</button>'}else if(id.status==='error'){body='<p>'+esc(id.message||'Pl@ntNet species identification was unavailable.')+'</p><button type="button" class="diagnose-secondary" onclick="window.GrowPlotCV.openPlantNetSettings()">Check species recognition settings</button>'}else{body='<p>No ranked species candidates were returned. Capture clear leaf, flower or fruit images and retry.</p>'}return '<section class="diagnosis-analysis provider-analysis cv-species-results"><h4>Plant species · Pl@ntNet image identification</h4>'+body+(Number.isFinite(Number(id.remainingIdentificationRequests))?'<small>Species-identification requests remaining today: '+esc(id.remainingIdentificationRequests)+'</small>':'')+'</section>'}
 function plantSpeciesMarkup(report,camera,plotId){let chosen=null;try{chosen=JSON.parse(localStorage.getItem('growplotSpecies:'+String(plotId||'')+':cctv'+Number(camera)||'null'))}catch(e){}if(chosen&&chosen.confirmedAt&&chosen.source!=='plantnet_image'){const esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])),at=(()=>{try{return new Intl.DateTimeFormat('en-MY',{timeZone:'Asia/Kuala_Lumpur',dateStyle:'medium',timeStyle:'short'}).format(new Date(chosen.confirmedAt))+' MYT'}catch(e){return chosen.confirmedAt}})();return '<section class="diagnosis-analysis provider-analysis cv-species-results"><h4>Plant identity confirmed</h4><p>'+esc(chosen.name||chosen.scientific)+' · '+esc(chosen.scientific||'')+'</p><p>Confirmed '+esc(at)+'. This identity is retained for this plot and CCTV section.</p></section>'}return plantSpeciesSelectionMarkup(report,camera,plotId)}
 function installSpeciesResults(){const markup=window.diagnosisProviderMarkup;if(typeof markup==='function'&&!markup.__growplotSpeciesWrapper){const wrapped=function(report,camera,plotId){return plantSpeciesMarkup(report,Number(camera)||0,String(plotId||''))+markup.call(this,report,camera,plotId)};wrapped.__growplotSpeciesWrapper=true;window.diagnosisProviderMarkup=wrapped}const original=window.onPlantNetDiagnosis;if(typeof original==='function'&&!original.__growplotSpeciesWrapper){const wrapped=function(camera,plotId,json){let report={};try{report=JSON.parse(json||'{}')}catch(e){}if(report.speciesIdentification){try{localStorage.setItem('growplotSpeciesCandidates:'+plotId+':cctv'+camera,JSON.stringify(report.speciesIdentification))}catch(e){}}return original.apply(this,arguments)};wrapped.__growplotSpeciesWrapper=true;window.onPlantNetDiagnosis=wrapped}}
 function confirmSpecies(camera,plotId,index){let identification;try{identification=JSON.parse(localStorage.getItem('growplotSpeciesCandidates:'+plotId+':cctv'+camera)||'null')}catch(e){}const row=identification&&Array.isArray(identification.results)?identification.results[index]:null;if(!row||!row.species)return;const species=row.species,scientific=species.scientificNameWithoutAuthor||species.scientificName||row.name||'',name=(Array.isArray(species.commonNames)&&species.commonNames[0])||scientific,score=Number(row.score),choice={name,scientific,score:Number.isFinite(score)?score:null,provider:'Pl@ntNet image identification'};if(typeof window.confirmDiagnosisSpeciesChoice==='function'){window.confirmDiagnosisSpeciesChoice(camera,choice);return}const confirmed={name,scientific,score:choice.score,source:choice.provider,confirmedAt:new Date().toISOString()};try{localStorage.setItem('growplotSpecies:'+plotId+':cctv'+camera,JSON.stringify(confirmed))}catch(e){}if(typeof window.openDiagnosis==='function')window.openDiagnosis(camera)}
 installSpeciesResults();
 window.GrowPlotCV={analyze:localAnalyze,localAnalyze:localAnalyze,runYolo,analyzeWithVision:visionAnalyze,confirmSpecies,openPlantNetSettings};
})();
