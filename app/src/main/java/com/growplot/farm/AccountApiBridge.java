package com.growplot.farm;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/** Firebase Auth + Firestore bridge for the existing GrowPlot WebView. */
public final class AccountApiBridge {
    private final Activity activity;
    private final WebView web;
    private final FirebaseAuth auth=FirebaseAuth.getInstance();
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    public AccountApiBridge(Activity activity, WebView web){this.activity=activity;this.web=web;}
    @JavascriptInterface public boolean configured(){return com.google.firebase.FirebaseApp.getApps(activity).size()>0;}
    @JavascriptInterface public boolean isDevelopmentBuild(){return BuildConfig.DEBUG;}
    @JavascriptInterface public boolean verifyLocalAdminTestLogin(){return BuildConfig.DEBUG;}
    @JavascriptInterface public void logout(){auth.signOut();}
    @JavascriptInterface public void startGoogleSignIn(){
        activity.runOnUiThread(()->{
            int resource=activity.getResources().getIdentifier("default_web_client_id","string",activity.getPackageName());
            if(resource==0){respond("googleSignIn",false,"Google sign-in is not configured in Firebase yet. Enable Google sign-in and add the Firebase OAuth client configuration, then rebuild.",null);return;}
            String webClientId=activity.getString(resource);
            if(webClientId==null||webClientId.trim().isEmpty()){respond("googleSignIn",false,"Google sign-in is not configured in Firebase yet. Add the Firebase web OAuth client ID, then rebuild.",null);return;}
            GetSignInWithGoogleOption googleOption=new GetSignInWithGoogleOption.Builder(webClientId).build();
            GetCredentialRequest request=new GetCredentialRequest.Builder().addCredentialOption(googleOption).build();
            CredentialManager manager=CredentialManager.create((Context)activity);
            manager.getCredentialAsync(activity,request,null,command->activity.runOnUiThread(command),new androidx.credentials.CredentialManagerCallback<GetCredentialResponse,GetCredentialException>(){
                @Override public void onResult(GetCredentialResponse result){
                    Credential credential=result.getCredential();
                    if(!(credential instanceof CustomCredential)||!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())){
                        respond("googleSignIn",false,"Google returned an unsupported sign-in credential.",null);return;
                    }
                    try{
                        GoogleIdTokenCredential google=GoogleIdTokenCredential.createFrom(((CustomCredential)credential).getData());
                        AuthCredential firebaseCredential=GoogleAuthProvider.getCredential(google.getIdToken(),null);
                        auth.signInWithCredential(firebaseCredential).addOnSuccessListener(resultAuth->{
                            FirebaseUser user=resultAuth.getUser();if(user==null){respond("googleSignIn",false,"Google sign-in returned no Firebase user.",null);return;}
                            ensureGoogleProfile(user);
                        }).addOnFailureListener(e->respond("googleSignIn",false,e.getMessage(),null));
                    }catch(Exception e){respond("googleSignIn",false,"Could not read the Google sign-in token: "+e.getMessage(),null);}
                }
                @Override public void onError(GetCredentialException e){
                    if(e instanceof androidx.credentials.exceptions.NoCredentialException){
                        respond("googleSignIn",false,"Google couldn’t find a usable account. Add a Google Account to this device or reauthenticate it in Android Settings, then try again.",null);
                    }else{
                        respond("googleSignIn",false,"Google sign-in failed: "+e.getMessage(),null);
                    }
                }
            });
        });
    }
    private void ensureGoogleProfile(FirebaseUser user){
        // Finish GrowPlot profile registration before the WebView starts first-user onboarding.
        if(user.getEmail()==null||user.getEmail().trim().isEmpty()){
            auth.signOut();
            respond("googleSignIn",false,"Google did not provide an email address, so GrowPlot could not register this user.",null);
            return;
        }
        com.google.firebase.firestore.DocumentReference profileRef=db.collection("users").document(user.getUid());
        Map<String,Object> profile=new HashMap<>();
        profile.put("email",user.getEmail());
        profile.put("role","USER");
        profile.put("plots",new java.util.ArrayList<>());
        profile.put("displayName",user.getDisplayName()==null?"":user.getDisplayName());
        profile.put("authProvider","google.com");
        profile.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
        db.runTransaction(transaction->{
            if(transaction.get(profileRef).exists())return false;
            transaction.set(profileRef,profile);
            return true;
        }).addOnSuccessListener(created->accountResponse("googleSignIn",user))
          .addOnFailureListener(e->failGoogleUserRegistration("could not create or check the GrowPlot user profile",e));
    }
    private void failGoogleUserRegistration(String reason,Exception error){
        auth.signOut();
        String detail=error==null||error.getMessage()==null?"Please check the connection and try again.":error.getMessage();
        respond("googleSignIn",false,"Google was authenticated, but GrowPlot "+reason+". Please try again. "+detail,null);
    }
    @JavascriptInterface public void request(String action,String payload){
        activity.runOnUiThread(()->execute(action,payload));
    }

    /** Save the Drive-relative folder path inside the existing dataFeed map.
     * Android's SAF URI and permission remain local to the device and are never synced. */
    public void saveImageFeedPath(String plotId,String displayPath,String relativePath){
        FirebaseUser user=auth.getCurrentUser();
        if(user==null){respond("saveFeedPath",false,"Sign in to save this default path to Firestore. The folder remains saved on this device.",null);return;}
        if(plotId==null||!plotId.matches("[A-Za-z0-9_-]{1,100}")||"plot-1".equals(plotId)||displayPath==null
                ||displayPath.trim().isEmpty()||displayPath.length()>512
                ||displayPath.startsWith("content://")){
            respond("saveFeedPath",false,"The selected folder path is invalid or this is the shared Demo Plot.",null);return;
        }
        Map<String,Object> updates=new HashMap<>();
        boolean validRelativePath=isValidRelativeFeedPath(relativePath);
        if(!validRelativePath){
            respond("saveFeedPath",false,"Choose a feed folder inside Google Drive → Projects → GrowPlot so its relative path can be shared with other devices. The folder remains saved on this device.",null);return;
        }
        updates.put("dataFeed.enabled",true);
        updates.put("dataFeed.relativePath",relativePath);
        updates.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
        db.collection("plots").document(user.getUid()+"_"+plotId).update(updates)
                .addOnSuccessListener(v->{
                    JSONObject result=new JSONObject();
                    try{result.put("plotId",plotId);result.put("displayPath",displayPath.trim());result.put("relativePath",relativePath);}catch(Exception ignored){}
                    respond("saveFeedPath",true,"",result);
                })
                .addOnFailureListener(e->respond("saveFeedPath",false,
                        "The folder is saved on this device, but Firestore could not save plots/"+user.getUid()+"_"+plotId+".dataFeed.relativePath: "+e.getMessage(),null));
    }

    private boolean isValidRelativeFeedPath(String path){
        if(path==null||path.length()>240||!path.matches("[A-Za-z0-9 _./-]{1,240}")||path.startsWith("/"))return false;
        for(String segment:path.split("/"))if(segment.isEmpty()||".".equals(segment)||"..".equals(segment))return false;
        return true;
    }

    private void execute(String action,String payload){
        try {
            JSONObject input=new JSONObject(payload==null?"{}":payload);
            if("passwordReset".equals(action)){
                String email=input.optString("email","").trim().toLowerCase(java.util.Locale.ROOT);
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    respond(action,false,"Enter a valid email address.",null);return;
                }
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(v->respond(action,true,"",null))
                    .addOnFailureListener(e->{
                        if(e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException){
                            respond(action,true,"",null);
                        }else{
                            respond(action,false,"Could not send a reset email. Please check your connection and try again.",null);
                        }
                    });
                return;
            }
            if("login".equals(action)||"register".equals(action)){
                String email=input.optString("email","").trim().toLowerCase(java.util.Locale.ROOT);
                String password=input.optString("password","");
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()||password.length()<6){
                    respond(action,false,"Enter a valid email and password (at least 6 characters).",null);return;
                }
                if("register".equals(action)){
                    auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(result->{
                        FirebaseUser user=result.getUser();
                        if(user==null){respond(action,false,"Registration returned no user.",null);return;}
                        Map<String,Object> profile=new HashMap<>();
                        profile.put("email",email);profile.put("role","USER");profile.put("plots",new java.util.ArrayList<>());
                        profile.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                        accountResponse(action,user);
                        db.collection("users").document(user.getUid()).set(profile)
                                .addOnFailureListener(e->reportAccountWarning("Account created, but the cloud profile could not be saved: "+e.getMessage()));
                    }).addOnFailureListener(e->{
                        if(e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException
                            || (e instanceof com.google.firebase.auth.FirebaseAuthException
                                && "ERROR_EMAIL_ALREADY_IN_USE".equals(((com.google.firebase.auth.FirebaseAuthException)e).getErrorCode()))){
                            JSONObject details=new JSONObject();
                            try{details.put("duplicateEmail",true);}catch(Exception ignored){}
                            respond(action,false,"This ID is already a registered member. Click Forgot password to set a new password.",details);
                        }else{
                            respond(action,false,e.getMessage(),null);
                        }
                    });
                }else auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(result->{
                    FirebaseUser user=result.getUser();
                    if(user!=null)accountResponse(action,user);else respond(action,false,"Login returned no user.",null);
                }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
                return;
            }
            FirebaseUser user=auth.getCurrentUser();
            if(user==null){respond(action,false,"Sign in first.",null);return;}
            if("loadOwnedDevices".equals(action)){
                String uid=user.getUid();
                db.collection("data_feeds").whereEqualTo("ownerUserId",uid).get().addOnSuccessListener(devices->{
                    db.collection("feed_assignments").whereEqualTo("ownerUserId",uid).get().addOnSuccessListener(assignments->{
                        try{
                            JSONObject result=new JSONObject();
                            JSONArray deviceArray=new JSONArray(),assignmentArray=new JSONArray();
                            for(com.google.firebase.firestore.QueryDocumentSnapshot item:devices){
                                JSONObject entry=new JSONObject();
                                entry.put("deviceId",item.getString("deviceId"));
                                entry.put("deviceName",item.getString("deviceName"));
                                entry.put("deviceType",item.getString("deviceType"));
                                entry.put("ipAddress",item.getString("ipAddress"));
                                entry.put("port",item.get("port"));
                                entry.put("protocol",item.getString("protocol"));
                                entry.put("streamPath",item.getString("streamPath"));
                                entry.put("username",item.getString("username"));
                                deviceArray.put(entry);
                            }
                            for(com.google.firebase.firestore.QueryDocumentSnapshot item:assignments){
                                JSONObject entry=new JSONObject();
                                for(String field:new String[]{"plotId","greenhouseId","sectionId","deviceId"})entry.put(field,item.getString(field));
                                assignmentArray.put(entry);
                            }
                            result.put("devices",deviceArray);result.put("assignments",assignmentArray);
                            respond(action,true,"",result);
                        }catch(Exception e){respond(action,false,e.getMessage(),null);}
                    }).addOnFailureListener(e->respond(action,false,"Assignment retrieval failed: "+e.getMessage(),null));
                }).addOnFailureListener(e->respond(action,false,"Device retrieval failed: "+e.getMessage(),null));
                return;
            }
            if("saveOwnedDevice".equals(action)){
                String deviceId=input.optString("deviceId",""), host=input.optString("ipAddress","");
                String type=input.optString("deviceType",""), password=input.optString("password","");
                int port=input.optInt("port",0);
                if(!deviceId.matches("[A-Za-z0-9_-]{1,100}")||!host.matches("[A-Za-z0-9.-]{1,253}")||
                    !("AV".equals(type)||"IoT".equals(type))||port<1||port>65535){
                    respond(action,false,"Invalid device configuration.",null);return;
                }
                Map<String,Object> record=new HashMap<>();
                record.put("deviceId",deviceId);
                record.put("ownerUserId",user.getUid());
                    record.put("ownerEmail",user.getEmail()==null?"":user.getEmail());
                record.put("deviceType",type);
                record.put("deviceName",input.optString("deviceName",deviceId));
                record.put("ipAddress",host);
                record.put("port",port);
                record.put("protocol",input.optString("protocol",""));
                record.put("streamPath",input.optString("streamPath",""));
                record.put("username",input.optString("username",""));
                record.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                if(!password.isEmpty()){
                    Map<String,Object> args=new com.google.gson.Gson().fromJson(input.toString(),java.util.Map.class);
                    FirebaseFunctions.getInstance("us-central1").getHttpsCallable("saveOwnedDevice").call(args)
                        .addOnSuccessListener(result->respond(action,true,"Device and encrypted password saved.",null))
                        .addOnFailureListener(e->{
                            // Cloud Functions may not be deployed. Save only non-secret device metadata.
                            // For the legacy VIGI CCTV03, keep credentials encrypted by Android Keystore.
                            // Never put the password in Firestore or logs.
                            boolean locallyProtected=false;
                            if("CCTV03".equals(deviceId)&&activity instanceof MainActivity){
                                locallyProtected=((MainActivity)activity).saveVigiCctv03Credentials(input.optString("username",""),password);
                            }
                            final boolean protectedOnDevice=locallyProtected;
                            db.collection("data_feeds").document(user.getUid()+"_"+deviceId).set(record,SetOptions.merge())
                              .addOnSuccessListener(v->respond(action,true,
                                protectedOnDevice?"Device saved. VIGI password protected on this device only; cloud encryption unavailable.":
                                "Device saved without cloud password. Enter camera credentials when connecting; cloud encryption unavailable.",null))
                              .addOnFailureListener(err->respond(action,false,"Device metadata save failed: "+err.getMessage(),null));
                        });
                }else{
                    db.collection("data_feeds").document(user.getUid()+"_"+deviceId).set(record,SetOptions.merge())
                        .addOnSuccessListener(v->respond(action,true,"Device saved to Firestore.",null))
                        .addOnFailureListener(e->respond(action,false,"Firestore device save failed: "+e.getMessage(),null));
                }
                return;
            }
            if("assignOwnedDevice".equals(action)){
                String plotId=input.optString("plotId",""),greenhouseId=input.optString("greenhouseId",""),sectionId=input.optString("sectionId",""),deviceId=input.optString("deviceId","");
                if(!plotId.matches("[A-Za-z0-9_-]{1,100}")||!greenhouseId.matches("greenhouse-[1-9][0-9]*")||!sectionId.matches("section-[1-9][0-9]*")||!deviceId.matches("[A-Za-z0-9_-]{1,100}")){
                    respond(action,false,"Select a valid plot, greenhouse, section and device.",null);return;
                }
                String uid=user.getUid();
                com.google.firebase.firestore.DocumentReference plotRef=db.collection("plots").document(uid+"_"+plotId);
                com.google.firebase.firestore.DocumentReference feedRef=db.collection("data_feeds").document(uid+"_"+deviceId);
                plotRef.get().addOnSuccessListener(plot->{
                    if(!plot.exists()||!uid.equals(plot.getString("ownerUserId"))){respond(action,false,"Plot not found in your Firebase account.",null);return;}
                    Number ghCount=plot.getDouble("greenhouseCount"),secCount=plot.getDouble("sectionsPerGreenhouse");
                    int gh=Integer.parseInt(greenhouseId.substring(11)),sec=Integer.parseInt(sectionId.substring(8));
                    if(ghCount==null||secCount==null||gh>ghCount.intValue()||sec>secCount.intValue()){
                        respond(action,false,"Greenhouse or section not configured in this plot. Save Plot Setup first.",null);return;
                    }
                    feedRef.get().addOnSuccessListener(feed->{
                        if(!feed.exists()||!uid.equals(feed.getString("ownerUserId"))){respond(action,false,"Device not found in Firebase. Save Device first.",null);return;}
                        Map<String,Object> record=new HashMap<>();
                        record.put("ownerUserId",uid);record.put("plotId",plotId);record.put("greenhouseId",greenhouseId);record.put("sectionId",sectionId);record.put("deviceId",deviceId);
                        record.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                        db.collection("feed_assignments").document(uid+"_"+plotId+"_"+greenhouseId+"_"+sectionId+"_"+deviceId).set(record,SetOptions.merge())
                            .addOnSuccessListener(v->respond(action,true,"Device assigned to section.",null))
                            .addOnFailureListener(e->respond(action,false,"Assignment write failed: "+e.getMessage(),null));
                    }).addOnFailureListener(e->respond(action,false,"Device lookup failed: "+e.getMessage(),null));
                }).addOnFailureListener(e->respond(action,false,"Plot lookup failed: "+e.getMessage(),null));
                return;
            }
            if("saveOwnedDevice".equals(action)||"assignOwnedDevice".equals(action)){
                String functionName=action;
                Map<String,Object> args=new com.google.gson.Gson().fromJson(input.toString(),java.util.Map.class);
                FirebaseFunctions.getInstance("us-central1").getHttpsCallable(functionName).call(args)
                    .addOnSuccessListener(result->{
                        JSONObject out=new JSONObject();
                        try{out.put("saved",true);}catch(Exception ignored){}
                        respond(functionName,true,"",out);
                    }).addOnFailureListener(e->respond(functionName,false,e.getMessage(),null));
                return;
            }
            if("saveVigiCloudCredentials".equals(action)){
                String username=input.optString("username","").trim(),password=input.optString("password","");
                if(username.isEmpty()||password.isEmpty()){respond(action,false,"Enter VIGI username and password.",null);return;}
                Map<String,Object> args=new HashMap<>();args.put("deviceId","CCTV03");args.put("username",username);args.put("password",password);
                FirebaseFunctions.getInstance("us-central1").getHttpsCallable("saveCameraCredentials").call(args)
                  .addOnSuccessListener(v->respond(action,true,"Encrypted VIGI credentials saved.",null))
                  .addOnFailureListener(e->respond(action,false,"Encrypted cloud credential save failed: "+e.getMessage(),null));
                return;
            }
            if("loadVigiCloudCredentials".equals(action)){
                Map<String,Object> args=new HashMap<>();args.put("deviceId","CCTV03");
                FirebaseFunctions.getInstance("us-central1").getHttpsCallable("loadCameraCredentials").call(args)
                  .addOnSuccessListener(result->{
                    try{
                        Map<?,?> data=(Map<?,?>)result.getData();
                        String username=(String)data.get("username"),password=(String)data.get("password");
                        if(username==null||password==null||!(activity instanceof MainActivity)||!((MainActivity)activity).saveVigiCctv03Credentials(username,password)){
                            respond(action,false,"Could not protect retrieved VIGI credentials on this device.",null);return;
                        }
                        JSONObject out=new JSONObject();out.put("username",username);
                        respond(action,true,"VIGI credentials restored securely.",out);
                    }catch(Exception e){respond(action,false,"Could not restore VIGI credentials.",null);}
                  }).addOnFailureListener(e->respond(action,false,"No cloud VIGI credentials available: "+e.getMessage(),null));
                return;
            }
            if("saveDataFeeds".equals(action)){
                JSONArray feeds=input.optJSONArray("feeds");
                if(feeds==null||feeds.length()!=3){respond(action,false,"Expected CCTV01–CCTV03.",null);return;}
                com.google.firebase.firestore.WriteBatch batch=db.batch();
                for(int i=0;i<3;i++){
                    JSONObject feed=feeds.optJSONObject(i);
                    String id="CCTV0"+(i+1);
                    if(feed==null||!id.equals(feed.optString("deviceId"))){respond(action,false,"Invalid CCTV device ID.",null);return;}
                    String ip=feed.optString("ipAddress","");
                    if(ip.length()>64||!ip.matches("[0-9.]*")){respond(action,false,"Invalid camera address.",null);return;}
                    Map<String,Object> record=new HashMap<>();
                    record.put("deviceId",id);record.put("deviceType","AV");record.put("deviceName",id);
                    record.put("ownerUserId",user.getUid());record.put("ipAddress",ip);
                    record.put("protocol",i==2?"RTSP":"HTTP");
                    record.put("port",i==2?554:8080);
                    record.put("streamPath",i==2?"/stream1":"/videofeed");
                    record.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                    // Passwords and stream credentials are deliberately excluded from Firestore.
                    batch.set(db.collection("users").document(user.getUid()).collection("data_feeds").document(id),record,SetOptions.merge());
                }
                batch.commit().addOnSuccessListener(v->respond(action,true,"CCTV01–CCTV03 saved to Firebase.",null))
                  .addOnFailureListener(e->respond(action,false,"Firebase device save failed: "+e.getMessage(),null));
                return;
            }
            if("listAccessUsers".equals(action)){
                db.collection("users").get().addOnSuccessListener(snapshot->{
                    try{
                        JSONArray users=new JSONArray();
                        for(com.google.firebase.firestore.DocumentSnapshot doc:snapshot.getDocuments()){
                            if(doc.getId().equals(user.getUid()))continue;
                            String email=doc.getString("email");
                            if(email==null||email.trim().isEmpty())continue;
                            JSONObject entry=new JSONObject();
                            entry.put("uid",doc.getId());entry.put("email",email);
                            users.put(entry);
                        }
                        JSONObject out=new JSONObject();out.put("users",users);
                        db.collection("users").document(user.getUid()).get().addOnSuccessListener(own->{
                            try{out.put("allowedMonitorUids",new JSONArray(new com.google.gson.Gson().toJson(
                                own.get("allowedMonitorUids")==null?new java.util.ArrayList<>():own.get("allowedMonitorUids"))));
                                respond(action,true,"",out);
                            }catch(Exception e){respond(action,false,e.getMessage(),null);}
                        }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
                    }catch(Exception e){respond(action,false,e.getMessage(),null);}
                }).addOnFailureListener(e->respond(action,false,
                    "Cannot list registered users. Firestore rules must authorize directory lookup: "+e.getMessage(),null));
                return;
            }
            if("saveAccessUsers".equals(action)){
                JSONArray selected=input.optJSONArray("uids");
                if(selected==null||selected.length()>200){respond(action,false,"Invalid monitoring access list.",null);return;}
                java.util.List<String> uids=new java.util.ArrayList<>();
                for(int i=0;i<selected.length();i++){
                    String uid=selected.optString(i,"");
                    if(!uid.matches("[A-Za-z0-9_-]{1,128}")||uid.equals(user.getUid())){
                        respond(action,false,"Invalid user selection.",null);return;
                    }
                    if(!uids.contains(uid))uids.add(uid);
                }
                db.collection("users").document(user.getUid()).update("allowedMonitorUids",uids)
                    .addOnSuccessListener(v->respond(action,true,"Monitoring access list saved.",null))
                    .addOnFailureListener(e->respond(action,false,"Firebase save failed: "+e.getMessage(),null));
                return;
            }
            if("profile".equals(action)){
                // Privileged plot discovery requires an authenticated Firebase custom claim,
                // never a locally editable role or power-user UI toggle.
                user.getIdToken(false).addOnSuccessListener(token->{
                    boolean powerUser="rizaljamin@gmail.com".equalsIgnoreCase(user.getEmail())
                        && (Boolean.TRUE.equals(token.getClaims().get("powerUser"))
                        || Boolean.TRUE.equals(token.getClaims().get("admin")));
                    db.collection("users").document(user.getUid()).get().addOnSuccessListener(profile->{
                    db.collection("plots").get()
                      .addOnSuccessListener(owned->{
                        JSONObject out=new JSONObject();
                        try{
                            JSONArray arr=new JSONArray();
                            for(com.google.firebase.firestore.DocumentSnapshot doc:owned.getDocuments()){
                                Map<String,Object> item=doc.getData();
                                if(item!=null)arr.put(new JSONObject(new com.google.gson.Gson().toJson(item)));
                            }
                            // Public plot visibility for authenticated users; writes remain owner-restricted.
                            out.put("plots",arr);
                            out.put("powerUser",powerUser);
                            String selected=profile.getString("selectedPlotId");
                            out.put("selectedPlotId",selected==null?JSONObject.NULL:selected);
                        }catch(Exception e){respond(action,false,"Invalid plot data: "+e.getMessage(),null);return;}
                        respond(action,true,"",out);
                      }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
                    }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
                }).addOnFailureListener(e->respond(action,false,"Could not verify account privileges: "+e.getMessage(),null));
            }else if("saveProfile".equals(action)){
                // Plots are independent top-level records; never embed plot data in users.
                JSONArray incoming=input.optJSONArray("plots");
                com.google.firebase.firestore.WriteBatch batch=db.batch();
                java.util.Set<String> incomingIds=new java.util.HashSet<>();
                if(incoming!=null)for(int i=0;i<incoming.length();i++){
                    JSONObject plot=incoming.optJSONObject(i);
                    if(plot==null)continue;
                    String plotId=plot.optString("id","");
                    if(!plotId.matches("[A-Za-z0-9_-]{1,100}")||"plot-1".equals(plotId)){
                        respond(action,false,"Invalid or reserved plot ID.",null);return;
                    }
                    incomingIds.add(plotId);
                    Map<String,Object> record=new com.google.gson.Gson().fromJson(plot.toString(),java.util.Map.class);
                    record.put("ownerUserId",user.getUid());
                    record.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                    batch.set(db.collection("plots").document(user.getUid()+"_"+plotId),record,SetOptions.merge());
                }
                Map<String,Object> profileUpdate=new HashMap<>();
                profileUpdate.put("selectedPlotId",input.optString("selectedPlotId",""));
                profileUpdate.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                batch.set(db.collection("users").document(user.getUid()),profileUpdate,SetOptions.merge());
                batch.commit()
                    .addOnSuccessListener(v->{JSONObject out=new JSONObject();try{out.put("saved",true);}catch(Exception ignored){}respond(action,true,"",out);})
                    .addOnFailureListener(e->respond(action,false,e.getMessage(),null));
            }else if("stats".equals(action))respond(action,false,"Administrator statistics require a secure server endpoint.",null);
            else respond(action,false,"Unsupported action.",null);
        }catch(Exception e){respond(action,false,e.getMessage(),null);}
    }
    private void accountResponse(String action,FirebaseUser user){
        JSONObject data=new JSONObject();JSONObject account=new JSONObject();
        try{
            account.put("uid",user.getUid());account.put("email",user.getEmail());
            if(user.getMetadata()!=null){
                account.put("registrationDate",user.getMetadata().getCreationTimestamp());
                account.put("lastLoginDate",user.getMetadata().getLastSignInTimestamp());
            }
            // Client never trusts a user-editable role; privileged features require server verification.
            account.put("role","USER");data.put("user",account);
        }catch(Exception ignored){}
        respond(action,true,"",data);
    }
    private void reportAccountWarning(String warning){
        JSONObject data=new JSONObject();
        try{data.put("warning",warning);}catch(Exception ignored){}
        respond("accountWarning",true,"",data);
    }
    private void respond(String action,boolean ok,String error,JSONObject data){
        activity.runOnUiThread(()->{
            try{
                JSONObject envelope=new JSONObject();envelope.put("action",action);envelope.put("ok",ok);
                envelope.put("error",error==null?"":error);envelope.put("data",data==null?JSONObject.NULL:data);
                web.evaluateJavascript("window.GrowPlotAccountResponse&&window.GrowPlotAccountResponse("+envelope.toString()+")",null);
            }catch(Exception ignored){}
        });
    }
}
