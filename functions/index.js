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
