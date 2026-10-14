(function(){
'use strict';
let view=null;
function showFirstPlotSetup(){
 const auth=document.getElementById('authScreen'),card=auth&&auth.querySelector('.auth-card');
 if(!auth||!card)return;
 auth.hidden=false;
 document.querySelectorAll('#authScreen .auth-view').forEach(node=>node.hidden=true);
 if(!view){
  view=document.createElement('section');view.id='growplotFirstPlotView';view.className='auth-view';view.setAttribute('aria-labelledby','growplotFirstPlotTitle');
  const title=document.createElement('h1');title.id='growplotFirstPlotTitle';title.textContent='Create your first plot';
  const intro=document.createElement('p');intro.className='auth-intro';intro.textContent='Give your farm a name to get started. You can add its location and other details later.';
  const form=document.createElement('form');form.id='growplotFirstPlotForm';
  const label=document.createElement('label');label.className='auth-field';label.textContent='Plot name';
  const input=document.createElement('input');input.name='plotName';input.type='text';input.required=true;input.maxLength=60;input.placeholder='e.g. Fertigation Jerkoh';input.autocomplete='organization';label.appendChild(input);
  const submit=document.createElement('button');submit.className='auth-primary';submit.type='submit';submit.textContent='Create plot';
  const status=document.createElement('p');status.id='growplotFirstPlotStatus';status.className='auth-status';status.setAttribute('role','status');status.setAttribute('aria-live','polite');
  form.append(label,submit,status);view.append(title,intro,form);card.appendChild(view);
  form.addEventListener('submit',e=>{
   e.preventDefault();const name=input.value.trim();if(!name){status.textContent='Enter a plot name.';return;}
   if(typeof plots==='undefined'||typeof savePlotData!=='function'){status.textContent='Plot setup is unavailable. Please try again.';return;}
   const number=String(Math.max(0,...plots.map(p=>Number(p.number)||0))+1);
   const id='user-plot-'+Date.now().toString(36)+'-'+Math.floor(Math.random()*10000).toString(36);
   const plot={id,number,name,address:'',latitude:'',longitude:'',description:'',dataFeed:{enabled:false,folderUri:null}};
   plots.push(plot);selectedPlotId=id;
   window.GrowPlotSuppressCloudSave=true;const saved=savePlotData();window.GrowPlotSuppressCloudSave=false;
   if(!saved){plots.pop();status.textContent='Could not save the plot on this device.';return;}
   if(window.GrowPlotBridge)GrowPlotBridge.ensurePlotFeed(id);
   if(typeof fillPlotSelectors==='function')fillPlotSelectors();if(typeof loadPlotForm==='function')loadPlotForm();
   status.textContent='Saving your plot to Firebase…';submit.disabled=true;
   if(typeof window.GrowPlotSaveCloudProfile!=='function'){submit.disabled=false;status.textContent='Your plot is on this device, but Firebase save is unavailable.';return;}
   window.GrowPlotSaveCloudProfile((ok,error)=>{
    if(!ok){submit.disabled=false;status.textContent='Your plot is on this device, but Firebase could not confirm the save. '+(error||'Please retry.');return;}
    auth.hidden=true;view.hidden=true;if(typeof window.returnHome==='function')window.returnHome();if(typeof render==='function')render();
   });
  });
 }
 view.hidden=false;const input=view.querySelector('input[name="plotName"]');if(input)input.focus();
}
window.GrowPlotNewUserPreview=showFirstPlotSetup;
})();
