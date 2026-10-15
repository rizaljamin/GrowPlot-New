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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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
    @JavascriptInterface public void logout(){auth.signOut();}
    @JavascriptInterface public void startGoogleSignIn(){
        activity.runOnUiThread(()->{
            int resource=activity.getResources().getIdentifier("default_web_client_id","string",activity.getPackageName());
            if(resource==0){respond("googleSignIn",false,"Google sign-in is not configured in Firebase yet. Enable Google sign-in and add the Firebase OAuth client configuration, then rebuild.",null);return;}
            String webClientId=activity.getString(resource);
            if(webClientId==null||webClientId.trim().isEmpty()){respond("googleSignIn",false,"Google sign-in is not configured in Firebase yet. Add the Firebase web OAuth client ID, then rebuild.",null);return;}
            GetGoogleIdOption googleOption=new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build();
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
                @Override public void onError(GetCredentialException e){respond("googleSignIn",false,e.getMessage(),null);}
            });
        });
    }
    private void ensureGoogleProfile(FirebaseUser user){
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot->{
            if(snapshot.exists()){accountResponse("googleSignIn",user);return;}
            Map<String,Object> profile=new HashMap<>();profile.put("email",user.getEmail());profile.put("role","USER");profile.put("plots",new java.util.ArrayList<>());profile.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
            db.collection("users").document(user.getUid()).set(profile).addOnSuccessListener(v->accountResponse("googleSignIn",user)).addOnFailureListener(e->respond("googleSignIn",false,"Google account was created, but the Firebase profile save failed: "+e.getMessage(),null));
        }).addOnFailureListener(e->respond("googleSignIn",false,"Unable to read the Firebase profile: "+e.getMessage(),null));
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
                        db.collection("users").document(user.getUid()).set(profile).addOnSuccessListener(v->accountResponse(action,user))
                          .addOnFailureListener(e->respond(action,false,"Account created, but profile save failed: "+e.getMessage(),null));
                    }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
                }else auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(result->{
                    FirebaseUser user=result.getUser();
                    if(user!=null)accountResponse(action,user);else respond(action,false,"Login returned no user.",null);
                }).addOnFailureListener(e->respond(action,false,e.getMessage(),null));
                return;
            }
            FirebaseUser user=auth.getCurrentUser();
            if(user==null){respond(action,false,"Sign in first.",null);return;}
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
                db.collection("users").document(user.getUid()).set(update,SetOptions.merge())
                    .addOnSuccessListener(v->{JSONObject out=new JSONObject();try{out.put("saved",true);}catch(Exception ignored){}respond(action,true,"",out);})
                    .addOnFailureListener(e->respond(action,false,e.getMessage(),null));
            }else if("stats".equals(action))respond(action,false,"Administrator statistics require a secure server endpoint.",null);
            else respond(action,false,"Unsupported action.",null);
        }catch(Exception e){respond(action,false,e.getMessage(),null);}
    }
    private void accountResponse(String action,FirebaseUser user){
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot->{
            JSONObject data=new JSONObject();JSONObject account=new JSONObject();
            try{
                account.put("uid",user.getUid());account.put("email",user.getEmail());
                // Client never trusts a user-editable role; privileged features require server verification.
                account.put("role","USER");data.put("user",account);
            }catch(Exception ignored){}
            respond(action,true,"",data);
        }).addOnFailureListener(e->respond(action,false,"Unable to read user profile: "+e.getMessage(),null));
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
