/* GrowPlot crop catalogue progressive disclosure, applied after the existing app scripts. */
(function(){
  if(typeof window.renderManageFarm!=='function') return;
  const original=window.renderManageFarm;
  let catalogueOpen=false;
  window.renderManageFarm=function(record){
    if(record && record.cropId) catalogueOpen=false;
    original.apply(this,arguments);
    const root=document.getElementById('manageFarmContent');
    if(!root || typeof farmActiveMenu==='undefined' || farmActiveMenu!=='crops') return;
    const section=root.querySelector('.farm-crop-selector');
    if(!section)return;
    section.hidden=!catalogueOpen;
    const button=document.createElement('button');
    button.type='button';button.className='farm-save';
    button.textContent=catalogueOpen?'Close crop catalogue':'Create Crop';
    button.setAttribute('aria-expanded',String(catalogueOpen));
    button.addEventListener('click',()=>{
      catalogueOpen=!catalogueOpen;
      section.hidden=!catalogueOpen;
      button.textContent=catalogueOpen?'Close crop catalogue':'Create Crop';
      button.setAttribute('aria-expanded',String(catalogueOpen));
      if(catalogueOpen)section.scrollIntoView({block:'nearest',behavior:'smooth'});
    });
    section.parentNode.insertBefore(button,section);
    root.querySelectorAll('[data-farm-action="assign"]').forEach(b=>b.addEventListener('click',()=>{catalogueOpen=false}));
  };
})();