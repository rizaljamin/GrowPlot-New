/**
 * GrowPlot central account service (deploy separately; NEVER bundle credentials in APK).
 * Requires Node 20+, npm install express googleapis, a Google service account with
 * restricted editor access to the GrowPlot/user directory, and HTTPS reverse proxy.
 * Environment: GOOGLE_APPLICATION_CREDENTIALS, GROWPLOT_USER_FOLDER_ID,
 * GROWPLOT_ACCOUNTS_FOLDER_ID, GROWPLOT_USERDATA_FOLDER_ID, PORT.
 * Do not expose this service without HTTPS, request throttling and audit logging.
 */
'use strict';
const express=require('express');
const {google}=require('googleapis');
const crypto=require('node:crypto');
const {Readable}=require('node:stream');
const app=express();
app.disable('x-powered-by');
app.use(express.json({limit:'16kb'}));
const drive=google.drive({version:'v3',auth:new google.auth.GoogleAuth({scopes:['https://www.googleapis.com/auth/drive']})});
const accounts=process.env.GROWPLOT_ACCOUNTS_FOLDER_ID;
const userdata=process.env.GROWPLOT_USERDATA_FOLDER_ID;
if(!accounts||!userdata)throw Error('Missing Drive folder IDs');
const hash=(password,salt)=>crypto.scryptSync(password,salt,64).toString('base64');
const tokenKey=crypto.randomBytes(32);
const sessions=new Map();
function sign(id,role){const t=crypto.randomBytes(32).toString('base64url');sessions.set(crypto.createHash('sha256').update(t).digest('hex'),{id,role,expires:Date.now()+3600000});return t}
function auth(req,res,next){const t=(req.get('authorization')||'').replace(/^Bearer /,'');const s=sessions.get(crypto.createHash('sha256').update(t).digest('hex'));if(!s||s.expires<Date.now())return res.status(401).json({error:'Not authenticated'});req.user=s;next()}
function validName(s){return typeof s==='string'&&/^[a-zA-Z0-9_.-]{3,64}$/.test(s)}
async function find(folder,name){const q=`'${folder}' in parents and name = '${name.replace(/'/g,"\\'")}' and trashed = false`;const r=await drive.files.list({q,fields:'files(id,name),nextPageToken',pageSize:10});return r.data.files[0]}
async function read(id){const r=await drive.files.get({fileId:id,alt:'media',responseType:'text'});return typeof r.data==='string'?JSON.parse(r.data):r.data}
async function write(folder,name,data){const body=JSON.stringify(data);const f=await find(folder,name);const media={mimeType:'application/json',body:Readable.from([body])};if(f)return drive.files.update({fileId:f.id,media,fields:'id'});return drive.files.create({requestBody:{name,parents:[folder],mimeType:'application/json'},media,fields:'id'})}
const publicProfile=a=>({userId:a.userId,username:a.username,role:a.role,createdAt:a.createdAt,lastLoginAt:a.lastLoginAt||null});
app.post('/v1/register',async(req,res)=>{try{const {username,password}=req.body;if(!validName(username)||typeof password!=='string'||password.length<12||password.length>256||username==='growplot_admin')return res.status(400).json({error:'Invalid username or password'});if(await find(accounts,username+'.json'))return res.status(409).json({error:'Username already exists'});const id=crypto.randomUUID();const salt=crypto.randomBytes(32).toString('base64');const a={schemaVersion:2,userId:id,username,role:'USER',passwordSalt:salt,passwordHash:hash(password,salt),createdAt:new Date().toISOString()};await write(accounts,username+'.json',a);await write(userdata,id+'.json',{userId:id,plots:[],settings:{},selectedPlotId:null});res.status(201).json({user:publicProfile(a),token:sign(id,'USER')});}catch(e){console.error('register error',e.message);res.status(503).json({error:'Account service unavailable'})}});
app.post('/v1/login',async(req,res)=>{try{const {username,password}=req.body;if(!validName(username)||typeof password!=='string')return res.status(401).json({error:'Invalid credentials'});const f=await find(accounts,username+'.json');if(!f)return res.status(401).json({error:'Invalid credentials'});const a=await read(f.id);const expected=Buffer.from(a.passwordHash||'','base64'),actual=Buffer.from(hash(password,a.passwordSalt),'base64');if(expected.length!==actual.length||!crypto.timingSafeEqual(expected,actual))return res.status(401).json({error:'Invalid credentials'});a.lastLoginAt=new Date().toISOString();await write(accounts,username+'.json',a);res.json({user:publicProfile(a),token:sign(a.userId,a.role)});}catch(e){console.error('login error',e.message);res.status(503).json({error:'Account service unavailable'})}});
app.get('/v1/me',auth,async(req,res)=>{try{const f=await find(userdata,req.user.id+'.json');res.json(f?await read(f.id):{userId:req.user.id,plots:[],settings:{}})}catch(e){res.status(503).json({error:'Drive unavailable'})}});
app.put('/v1/me',auth,async(req,res)=>{try{const {plots,settings,selectedPlotId}=req.body;if(!Array.isArray(plots)||plots.length>100||typeof settings!=='object'||settings===null)return res.status(400).json({error:'Invalid profile'});const p={userId:req.user.id,plots,settings,selectedPlotId:selectedPlotId||null};await write(userdata,req.user.id+'.json',p);res.json({saved:true})}catch(e){res.status(503).json({error:'Drive unavailable'})}});
app.get('/v1/admin/stats',auth,async(req,res)=>{if(req.user.role!=='ADMIN')return res.status(403).json({error:'Forbidden'});try{let token,users=0,admins=0,last7Days=0;do{const r=await drive.files.list({q:`'${accounts}' in parents and trashed = false`,fields:'nextPageToken,files(id,name)',pageSize:1000,pageToken:token});for(const f of r.data.files){const a=await read(f.id);users++;if(a.role==='ADMIN')admins++;if(a.lastLoginAt&&Date.now()-Date.parse(a.lastLoginAt)<7*86400000)last7Days++}token=r.data.nextPageToken}while(token);res.json({totalUsers:users,standardUsers:users-admins,administrators:admins,loggedInLast7Days:last7Days})}catch(e){res.status(503).json({error:'Drive unavailable'})}});
app.listen(Number(process.env.PORT||8080),()=>console.log('GrowPlot account API ready'));
