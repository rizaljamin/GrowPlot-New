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
            if("profile".equals(action)){
                db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot->{
                    JSONObject out=new JSONObject();
                    try{
                        Object plots=snapshot.get("plots");
                        out.put("plots",new JSONArray(plots==null?"[]":new com.google.gson.Gson().toJson(plots)));
                        out.put("selectedPlotId",snapshot.getString("selectedPlotId")==null?JSONObject.NULL:snapshot.getString("selectedPlotId"));
                    }catch(Exception ignored){}
                    respond(action,true,"",out);
                }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
            }else if("saveProfile".equals(action)){
                Map<String,Object> update=new HashMap<>();
                update.put("plots",new com.google.gson.Gson().fromJson(input.optJSONArray("plots")==null?"[]":input.optJSONArray("plots").toString(),java.util.List.class));
                update.put("selectedPlotId",input.optString("selectedPlotId",""));
                update.put("settings",new com.google.gson.Gson().fromJson(input.optJSONObject("settings")==null?"{}":input.optJSONObject("settings").toString(),java.util.Map.class));
                update.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
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
                    batch.set(db.collection("plots").document(plotId),record,SetOptions.merge());
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
