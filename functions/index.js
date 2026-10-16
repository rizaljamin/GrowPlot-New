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
