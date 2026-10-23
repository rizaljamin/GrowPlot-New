import {onCall, HttpsError} from 'firebase-functions/v2/https';
import {initializeApp} from 'firebase-admin/app';
import {getFirestore, FieldValue} from 'firebase-admin/firestore';
import {KeyManagementServiceClient} from '@google-cloud/kms';
import {GoogleAuth} from 'google-auth-library';
import {Readable} from 'node:stream';
import {createInterface} from 'node:readline';
import {parseCsvLine, pricePerKg, monthlyPriceSummary} from './pricecatcher-utils.js';
initializeApp();
const db=getFirestore(), kms=new KeyManagementServiceClient();
const key=process.env.GROWPLOT_KMS_KEY_NAME;
function authorize(req){
 if(!req.auth?.uid)throw new HttpsError('unauthenticated','Firebase sign-in required');
 if(!key)throw new HttpsError('failed-precondition','KMS key not configured');
 return req.auth.uid;
}
function ref(uid,id){
 if(!/^CCTV0[1-3]$/.test(id||''))throw new HttpsError('invalid-argument','Unsupported camera ID');
 return db.collection('users').doc(uid).collection('data_feeds').doc(id);
}
export const saveCameraCredentials=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const uid=authorize(req), {deviceId,username,password}=req.data||{};
 if(typeof username!=='string'||!username.trim()||username.length>128||typeof password!=='string'||!password||password.length>256)throw new HttpsError('invalid-argument','Username and password required');
 const [encrypted]=await kms.encrypt({name:key,plaintext:Buffer.from(password,'utf8')});
 if(!encrypted.ciphertext)throw new HttpsError('internal','Encryption failed');
 await ref(uid,deviceId).set({deviceId,deviceType:'AV',ownerUserId:uid,username:username.trim(),credentials:{algorithm:'GOOGLE_CLOUD_KMS',keyName:key,ciphertext:Buffer.from(encrypted.ciphertext).toString('base64')},updatedAt:FieldValue.serverTimestamp()},{merge:true});
 return {saved:true,deviceId};
});
export const loadCameraCredentials=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const uid=authorize(req), deviceId=req.data?.deviceId;
 const snap=await ref(uid,deviceId).get();
 if(!snap.exists||!snap.get('credentials.ciphertext'))throw new HttpsError('not-found','No saved credentials');
 const stored=snap.get('credentials');
 if(stored.keyName!==key)throw new HttpsError('failed-precondition','Key version unavailable');
 const [decrypted]=await kms.decrypt({name:key,ciphertext:Buffer.from(stored.ciphertext,'base64')});
 return {deviceId,username:snap.get('username')||'',password:Buffer.from(decrypted.plaintext).toString('utf8')};
});

function safeId(value,label){
 if(typeof value!=='string'||!/^[A-Za-z0-9_-]{1,100}$/.test(value))throw new HttpsError('invalid-argument','Invalid '+label);
 return value;
}
function ownerPlotId(uid,plotId){return uid+'_'+safeId(plotId,'plot ID');}
function feedDocId(uid,deviceId){return uid+'_'+safeId(deviceId,'device ID');}
export const saveOwnedDevice=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const uid=authorize(req);
 const {deviceId,deviceType,deviceName,ipAddress,port,protocol,streamPath,username,password}=req.data||{};
 safeId(deviceId,'device ID');
 if(!['AV','IoT'].includes(deviceType))throw new HttpsError('invalid-argument','Invalid device type');
 if(typeof ipAddress!=='string'||ipAddress.length>253||!(/^[a-zA-Z0-9.-]+$/.test(ipAddress)))throw new HttpsError('invalid-argument','Invalid host');
 if(!Number.isInteger(port)||port<1||port>65535)throw new HttpsError('invalid-argument','Invalid port');
 if(typeof username!=='undefined'&&(typeof username!=='string'||username.length>128))throw new HttpsError('invalid-argument','Invalid username');
 if(typeof password!=='undefined'&&(typeof password!=='string'||password.length>256))throw new HttpsError('invalid-argument','Invalid password');
 const doc=db.collection('data_feeds').doc(feedDocId(uid,deviceId));
 const existing=await doc.get();
 if(existing.exists&&existing.get('ownerUserId')!==uid)throw new HttpsError('permission-denied','Device belongs to another user');
 const data={deviceId,ownerUserId:uid,deviceType,deviceName:String(deviceName||deviceId).slice(0,100),ipAddress,port,protocol:String(protocol||'').slice(0,20),streamPath:String(streamPath||'').slice(0,300),updatedAt:FieldValue.serverTimestamp()};
 if(username!==undefined)data.username=username;
 if(password){const [encrypted]=await kms.encrypt({name:key,plaintext:Buffer.from(password,'utf8')});data.credentials={algorithm:'GOOGLE_CLOUD_KMS',keyName:key,ciphertext:Buffer.from(encrypted.ciphertext).toString('base64')};}
 await doc.set(data,{merge:true});
 return {saved:true,deviceId};
});
export const assignOwnedDevice=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const uid=authorize(req),{plotId,sectionId,deviceId,greenhouseId}=req.data||{};
 safeId(plotId,'plot ID');safeId(sectionId,'section ID');safeId(deviceId,'device ID');
 if(greenhouseId)safeId(greenhouseId,'greenhouse ID');
 const plot=await db.collection('plots').doc(ownerPlotId(uid,plotId)).get();
 const device=await db.collection('data_feeds').doc(feedDocId(uid,deviceId)).get();
 if(!plot.exists||plot.get('ownerUserId')!==uid)throw new HttpsError('permission-denied','Plot not owned by signed-in user');
 if(!device.exists||device.get('ownerUserId')!==uid)throw new HttpsError('permission-denied','Device not owned by signed-in user');
 const id=uid+'_'+plotId+'_'+sectionId+'_'+deviceId;
 await db.collection('feed_assignments').doc(id).set({ownerUserId:uid,plotId,sectionId,greenhouseId:greenhouseId||'',deviceId,updatedAt:FieldValue.serverTimestamp()},{merge:true});
 return {assigned:true,deviceId,plotId,sectionId};
});


// Authenticated Vertex image diagnosis. Images are sent inline and are not stored by this function.
const VERTEX_PROJECT_ID = process.env.GCLOUD_PROJECT || 'growplot';
const VERTEX_CALLABLE_REGION = 'us-central1';
// Gemini 3.5 Flash is served from the US multi-region, not us-central1.
const VERTEX_LOCATION = 'us';
const VERTEX_MODEL = 'gemini-3.5-flash';
const VERTEX_RUNTIME_SERVICE_ACCOUNT = 'growplot-vertex-api@growplot.iam.gserviceaccount.com';
const vertexGoogleAuth = new GoogleAuth({
  scopes: ['https://www.googleapis.com/auth/cloud-platform']
});

function diagnosisImages(data) {
  const images = Array.isArray(data?.images)
    ? data.images
    : (data?.imageBase64 ? [{mimeType: data.mimeType || 'image/jpeg', data: data.imageBase64}] : []);
  if (images.length < 1 || images.length > 5) {
    throw new HttpsError('invalid-argument', 'Send between 1 and 5 plant photos.');
  }
  let totalEncodedBytes = 0;
  return images.map((item, index) => {
    const mimeType = String(item?.mimeType || '').toLowerCase();
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(mimeType)) {
      throw new HttpsError('invalid-argument', 'Photo ' + (index + 1) + ' must be JPEG, PNG, or WebP.');
    }
    let encoded = String(item?.data || '').trim();
    encoded = encoded.replace(/^data:image\/(?:jpeg|png|webp);base64,/i, '').replace(/\s+/g, '');
    if (!encoded || !/^[A-Za-z0-9+/]+={0,2}$/.test(encoded) || encoded.length % 4 === 1) {
      throw new HttpsError('invalid-argument', 'Photo ' + (index + 1) + ' is not valid base64 image data.');
    }
    totalEncodedBytes += encoded.length;
    if (totalEncodedBytes > 7_000_000) {
      throw new HttpsError('invalid-argument', 'Compress the selected photos to a combined size below 5 MB.');
    }
    return {mimeType, data: encoded};
  });
}

function diagnosisPrompt(context) {
  const species = typeof context?.species === 'string' ? context.species.slice(0, 120) : '';
  const symptoms = typeof context?.symptoms === 'string' ? context.symptoms.slice(0, 500) : '';
  return [
    'Analyze the attached crop or plant photos for a practical visual health assessment.',
    'Only estimate color proportions within visible plant foliage; exclude soil, pot, background, and other objects.',
    'Return valid JSON only, with no Markdown, using this structure:',
    '{"plant_visible":true,"plant_description":"...","color_breakdown_percent":{"green":0,"yellow":0,"brown":0},"health_assessment":"...","observed_signs":["..."],"confidence":"low|medium|high","limitations":"..."}',
    'The green, yellow, and brown percentages must total 100 and are visual estimates, not pixel measurements. Say when foliage is obscured or too small. Do not claim a specific disease unless the photos support it; describe uncertainty.',
    species ? 'User-confirmed species: ' + species : '',
    symptoms ? 'User-reported symptom: ' + symptoms : ''
  ].filter(Boolean).join('\n');
}

export const vertexImageDiagnosis = onCall({
  region: VERTEX_CALLABLE_REGION,
  timeoutSeconds: 120,
  memory: '1GiB',
  maxInstances: 3,
  enforceAppCheck: false,
  serviceAccount: VERTEX_RUNTIME_SERVICE_ACCOUNT
}, async req => {
  if (!req.auth?.uid) {
    throw new HttpsError('unauthenticated', 'Sign in to GrowPlot before running Vertex analysis.');
  }
  const images = diagnosisImages(req.data || {});
  let accessToken;
  try {
    const client = await vertexGoogleAuth.getClient();
    const tokenResult = await client.getAccessToken();
    accessToken = typeof tokenResult === 'string' ? tokenResult : tokenResult?.token;
    if (!accessToken) throw new Error('No OAuth access token was returned.');
  } catch (error) {
    console.error('Vertex authentication failed:', error?.message || error);
    throw new HttpsError('internal', 'Vertex authentication failed. Check the Cloud Function service identity.');
  }

  const endpoint = 'https://aiplatform.' + VERTEX_LOCATION + '.rep.googleapis.com/v1/projects/'
    + encodeURIComponent(VERTEX_PROJECT_ID) + '/locations/' + VERTEX_LOCATION
    + '/publishers/google/models/' + VERTEX_MODEL + ':generateContent';
  const parts = images.map(image => ({inlineData: image}));
  parts.push({text: diagnosisPrompt(req.data?.context || {})});
  let response;
  try {
    response = await fetch(endpoint, {
      method: 'POST',
      headers: {'Authorization': 'Bearer ' + accessToken, 'Content-Type': 'application/json'},
      body: JSON.stringify({
        contents: [{role: 'user', parts}],
        generationConfig: {responseMimeType: 'application/json', temperature: 0.2}
      }),
      signal: AbortSignal.timeout(110_000)
    });
  } catch (error) {
    console.error('Vertex request transport failed:', error?.message || error);
    throw new HttpsError('unavailable', 'Could not reach Vertex AI. Please try again.');
  }
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    const detail = String(payload?.error?.message || '').slice(0, 240);
    console.error('Vertex returned HTTP', response.status, detail);
    throw new HttpsError(
      response.status === 429 ? 'resource-exhausted' : 'unavailable',
      'Vertex AI returned HTTP ' + response.status + (detail ? ': ' + detail : '')
    );
  }
  const outputText = (payload?.candidates?.[0]?.content?.parts || [])
    .map(part => part?.text || '').join('').trim();
  if (!outputText) {
    throw new HttpsError('internal', 'Vertex returned no image-analysis text.');
  }
  let analysis;
  try {
    analysis = JSON.parse(outputText);
  } catch (error) {
    console.error('Vertex returned invalid JSON:', error?.message || error);
    throw new HttpsError('internal', 'Vertex returned an unreadable analysis. Run diagnosis again.');
  }
  return {
    status: 'ok',
    provider: 'Vertex AI',
    model: VERTEX_MODEL,
    generatedAt: new Date().toISOString(),
    analysis
  };
});


const PRICE_DATA_ROOT='https://storage.data.gov.my/pricecatcher';
const priceCacheLoads=new Map();
let itemLookupPromise;
function currentPriceMonth(){
 const now=new Date();
 return now.toISOString().slice(0,7);
}
async function csvRows(url,onRow){
 let response;
 try { response=await fetch(url,{signal:AbortSignal.timeout(480000)}); }
 catch(error){ console.error('PriceCatcher download failed:',error?.message||error); throw new HttpsError('unavailable','Could not reach Malaysia PriceCatcher data. Try again later.'); }
 if(!response.ok) throw new HttpsError(response.status===404?'not-found':'unavailable','PriceCatcher has no downloadable data for this month (HTTP '+response.status+').');
 const length=Number(response.headers.get('content-length')||0);
 if(length>500_000_000) throw new HttpsError('resource-exhausted','PriceCatcher monthly file is larger than the supported limit.');
 const reader=createInterface({input:Readable.fromWeb(response.body),crlfDelay:Infinity});
 let headers=null;
 for await(const line of reader){
  if(!headers){headers=parseCsvLine(line).map(h=>h.replace(/^\uFEFF/,'').trim());continue;}
  if(!line.trim())continue;
  const cells=parseCsvLine(line),row={};
  headers.forEach((name,index)=>{row[name]=cells[index]??''});
  onRow(row);
 }
 if(!headers) throw new HttpsError('unavailable','PriceCatcher returned an empty CSV file.');
}
async function loadItemLookup(){
 if(!itemLookupPromise){
  itemLookupPromise=(async()=>{
   const items=new Map();
   await csvRows(PRICE_DATA_ROOT+'/lookup_item.csv',row=>{
    const code=String(row.item_code||'').trim(),item=String(row.item||'').trim(),unit=String(row.unit||'').trim();
    if(code&&item)items.set(code,{item,unit});
   });
   if(!items.size)throw new HttpsError('unavailable','PriceCatcher item lookup contained no products.');
   return items;
  })().catch(error=>{itemLookupPromise=null;throw error});
 }
 return itemLookupPromise;
}
async function buildMonthStats(month){
 const items=await loadItemLookup(),stats=new Map();
 const sourceUrl=PRICE_DATA_ROOT+'/pricecatcher_'+month+'.csv';
 await csvRows(sourceUrl,row=>{
  const code=String(row.item_code||'').trim(),item=items.get(code);
  if(!item)return;
  const perKg=pricePerKg(row.price,item.unit);
  if(perKg===null)return;
  let entry=stats.get(code);
  if(!entry){entry={item:item.item,unit:item.unit,sum:0,count:0};stats.set(code,entry)}
  entry.sum+=perKg;
  entry.count+=1;
 });
 const result=[...stats.values()].filter(row=>row.count>0);
 if(!result.length)throw new HttpsError('not-found','PriceCatcher returned no per-kilogram observations for '+month+'.');
 if(Buffer.byteLength(JSON.stringify(result),'utf8')>900_000)throw new HttpsError('resource-exhausted','Monthly price summary is too large to cache.');
 return {sourceUrl,items:result,fetchedAtMs:Date.now()};
}
async function getCachedMonthStats(month){
 const cacheRef=db.collection('public_market_price_cache').doc(month);
 const snap=await cacheRef.get(),current=month===currentPriceMonth();
 if(snap.exists){
  const value=snap.data()||{},age=Date.now()-Number(value.fetchedAtMs||0);
  if(Array.isArray(value.items)&&value.sourceUrl&&(!current||age<6*60*60*1000))return value;
 }
 if(priceCacheLoads.has(month))return priceCacheLoads.get(month);
 const load=buildMonthStats(month).then(async result=>{
  await cacheRef.set(result);
  return result;
 }).finally(()=>priceCacheLoads.delete(month));
 priceCacheLoads.set(month,load);
 return load;
}
export const getMonthlyRetailCropPrices=onCall({
 region:'us-central1',
 timeoutSeconds:540,
 memory:'1GiB',
 maxInstances:2,
 enforceAppCheck:false
},async req=>{
 if(!req.auth?.uid)throw new HttpsError('unauthenticated','Sign in to GrowPlot to load public market prices.');
 const month=String(req.data?.month||'');
 if(!/^\d{4}-(0[1-9]|1[0-2])$/.test(month)||month>currentPriceMonth())throw new HttpsError('invalid-argument','Choose a valid month that is not in the future.');
 const aliases=Array.isArray(req.data?.aliases)?req.data.aliases.slice(0,8).map(value=>String(value||'').replace(/[^A-Za-z0-9À-ÿ -]/g,' ').replace(/\s+/g,' ').trim()).filter(value=>value.length>=3&&value.length<=60):[];
 if(!aliases.length)throw new HttpsError('invalid-argument','Choose a crop with a searchable name.');
 const cached=await getCachedMonthStats(month);
 return monthlyPriceSummary(month,cached.sourceUrl,cached.items,aliases);
});
