// Pure, testable projection from Firebase Demo Plot records to public, read-only stream metadata.
// Never include cloud credentials, owner IDs, email addresses or unrelated private plots.
const validId=value=>typeof value==='string'&&/^[A-Za-z0-9_-]{1,100}$/.test(value);
const validHost=value=>typeof value==='string'&&/^[A-Za-z0-9.-]{1,253}$/.test(value);
const cappedCount=value=>Number.isInteger(Number(value))&&Number(value)>0?Math.min(Number(value),100):0;

export function projectDemoStreamConfig(plot,assignmentRows,deviceLookup,greenhouseId='greenhouse-1'){
 const greenhouseCount=cappedCount(plot?.greenhouseCount);
 const sectionsPerGreenhouse=cappedCount(plot?.sectionsPerGreenhouse);
 if(!/^greenhouse-[1-9][0-9]*$/.test(greenhouseId)||Number(greenhouseId.slice(11))>greenhouseCount)
  return {plotId:'plot-1',greenhouseId,greenhouseCount,sectionsPerGreenhouse:0,streams:[]};
 const seenSections=new Set(),streams=[];
 const sorted=[...(Array.isArray(assignmentRows)?assignmentRows:[])].sort((a,b)=>{
  const timestamp=value=>typeof value?.toMillis==='function'?value.toMillis():new Date(value||0).getTime()||0;
  return timestamp(b?.updatedAt)-timestamp(a?.updatedAt);
 });
 for(const row of sorted){
  if(row?.plotId!=='plot-1'||row?.greenhouseId!==greenhouseId||!validId(row.deviceId)||!validId(row.ownerUserId))continue;
  const match=/^section-([1-9][0-9]*)$/.exec(String(row.sectionId||''));
  if(!match)continue;
  const section=Number(match[1]);
  if(section<1||section>Math.min(sectionsPerGreenhouse,3)||seenSections.has(section))continue;
  const d=deviceLookup?.[row.ownerUserId+'_'+row.deviceId];
  if(!d||d.ownerUserId!==row.ownerUserId||d.deviceId!==row.deviceId||!['AV','Camera'].includes(d.deviceType)||!validHost(d.ipAddress))continue;
  const protocol=String(d.protocol||'').toUpperCase();
  if(!['HTTP','HTTPS','RTSP'].includes(protocol))continue;
  const port=Number(d.port);
  if(!Number.isInteger(port)||port<1||port>65535)continue;
  const path=String(d.streamPath|| (protocol==='RTSP'?'/stream1':'/videofeed'));
  if(!path.startsWith('/')||path.includes('..')||path.includes('@')||path.includes('?')||path.includes('#')||/\s/.test(path))continue;
  const scheme=protocol.toLowerCase();
  streams.push({sectionId:'section-'+section,deviceId:row.deviceId,streamUrl:scheme+'://'+d.ipAddress+':'+port+path});
  seenSections.add(section);
 }
 streams.sort((a,b)=>Number(a.sectionId.slice(8))-Number(b.sectionId.slice(8)));
 return {plotId:'plot-1',greenhouseId,greenhouseCount,sectionsPerGreenhouse,streams};
}
