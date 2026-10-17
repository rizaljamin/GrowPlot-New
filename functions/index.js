import {onCall, HttpsError} from 'firebase-functions/v2/https';
import {initializeApp} from 'firebase-admin/app';
import {getFirestore, FieldValue} from 'firebase-admin/firestore';
import {KeyManagementServiceClient} from '@google-cloud/kms';
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

// Plot video grants are owner-controlled and never expose encrypted camera credentials.
function plotGrantRef(ownerUid,plotId,viewerUid){return db.collection('plot_video_grants').doc(ownerUid+'_'+safeId(plotId,'plot ID')+'_'+viewerUid);}
function requireUid(value){if(typeof value!=='string'||!value||value.length>128||!/^[A-Za-z0-9_-]+$/.test(value))throw new HttpsError('invalid-argument','Invalid user ID');return value;}
async function ownedPlot(uid,plotId){const p=await db.collection('plots').doc(ownerPlotId(uid,plotId)).get();if(!p.exists||p.get('ownerUserId')!==uid)throw new HttpsError('permission-denied','Plot ownership could not be verified');return p;}
export const grantPlotVideo=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const ownerUid=requireUid(req.auth?.uid),plotId=safeId(req.data?.plotId,'plot ID');
 await ownedPlot(ownerUid,plotId);
 const email=String(req.data?.email||'').trim().toLowerCase();
 if(email.length>254||!/^\S+@\S+\.\S+$/.test(email))throw new HttpsError('invalid-argument','Valid registered email required');
 const users=await db.collection('users').where('email','==',email).limit(2).get();
 // Do not enumerate the directory or reveal whether an email has an account.
 if(users.size!==1)throw new HttpsError('failed-precondition','Recipient cannot be granted access. Confirm their GrowPlot registration email.');
 const viewerUid=users.docs[0].id;if(viewerUid===ownerUid)throw new HttpsError('invalid-argument','You already own this plot');
 await plotGrantRef(ownerUid,plotId,viewerUid).set({ownerUid,plotId,viewerUid,viewerEmail:email,active:true,updatedAt:FieldValue.serverTimestamp()},{merge:true});
 return {saved:true};
});
export const revokePlotVideo=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const ownerUid=requireUid(req.auth?.uid),plotId=safeId(req.data?.plotId,'plot ID'),viewerUid=requireUid(req.data?.viewerUid);
 await ownedPlot(ownerUid,plotId);
 await plotGrantRef(ownerUid,plotId,viewerUid).set({active:false,updatedAt:FieldValue.serverTimestamp()},{merge:true});
 return {revoked:true};
});
export const listPlotVideoAccess=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const uid=requireUid(req.auth?.uid);
 const [owned,received]=await Promise.all([db.collection('plot_video_grants').where('ownerUid','==',uid).get(),db.collection('plot_video_grants').where('viewerUid','==',uid).where('active','==',true).get()]);
 const grants=[];for(const doc of owned.docs){const d=doc.data();grants.push({plotId:d.plotId,viewerUid:d.viewerUid,viewerEmail:d.viewerEmail||'',active:d.active===true});}
 const shared=[];for(const doc of received.docs){const d=doc.data();const plot=await db.collection('plots').doc(ownerPlotId(d.ownerUid,d.plotId)).get();if(plot.exists&&plot.get('ownerUserId')===d.ownerUid)shared.push({plotId:d.plotId,plotName:plot.get('name')||d.plotId,ownerUid:d.ownerUid,ownerEmail:plot.get('ownerEmail')||''});}
 return {grants,shared};
});
export const resolvePlotVideo=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const viewerUid=requireUid(req.auth?.uid),ownerUid=requireUid(req.data?.ownerUid),plotId=safeId(req.data?.plotId,'plot ID'),sectionId=safeId(req.data?.sectionId,'section ID');
 const greenhouseId=req.data?.greenhouseId?safeId(req.data.greenhouseId,'greenhouse ID'):'';
 await ownedPlot(ownerUid,plotId);
 if(viewerUid!==ownerUid){const grant=await plotGrantRef(ownerUid,plotId,viewerUid).get();if(!grant.exists||grant.get('active')!==true)throw new HttpsError('permission-denied','Plot video viewing access not granted');}
 const assignments=await db.collection('feed_assignments').where('ownerUserId','==',ownerUid).where('plotId','==',plotId).where('sectionId','==',sectionId).get();
 const streams=[];
 for(const a of assignments.docs){const d=a.data();if((d.greenhouseId||'')!==greenhouseId)continue;const feed=await db.collection('data_feeds').doc(feedDocId(ownerUid,d.deviceId)).get();if(!feed.exists||feed.get('ownerUserId')!==ownerUid)continue;
 // No password, KMS ciphertext or camera administrative username is sent to a shared viewer.
 streams.push({deviceId:d.deviceId,deviceName:feed.get('deviceName')||d.deviceId,protocol:feed.get('protocol')||'',host:feed.get('ipAddress')||'',port:feed.get('port')||null,path:feed.get('streamPath')||''});
 }
 return {ownerUid,plotId,greenhouseId,sectionId,streams};
});

export const listAccessUsers=onCall({region:'us-central1',enforceAppCheck:false},async req=>{
 const uid=req.auth?.uid;
 if(!uid)throw new HttpsError('unauthenticated','Sign in to view registered users');
 const [directory,own]=await Promise.all([
  db.collection('users').orderBy('email').limit(200).get(),
  db.collection('users').doc(uid).get()
 ]);
 const users=directory.docs.filter(d=>d.id!==uid&&typeof d.get('email')==='string'&&d.get('email').trim())
  .map(d=>({uid:d.id,email:d.get('email')}));
 const allowedMonitorUids=own.exists&&Array.isArray(own.get('allowedMonitorUids'))?own.get('allowedMonitorUids'):[];
 return {users,allowedMonitorUids};
});
