package com.growplot.farm;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    @JavascriptInterface public boolean configured(){return true;}
    @JavascriptInterface public void logout(){auth.signOut();}
    @JavascriptInterface public void request(String action,String payload){
        activity.runOnUiThread(()->execute(action,payload));
    }
    private void execute(String action,String payload){
        try {
            JSONObject input=new JSONObject(payload==null?"{}":payload);
            if("login".equals(action)||"register".equals(action)){
                String email=input.optString("email",input.optString("username","")).trim().toLowerCase(java.util.Locale.ROOT);
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
                        out.put("selectedPlotId",snapshot.getString("selectedPlotId"));
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
            }else if("stats")respond(action,false,"Administrator statistics require a secure server endpoint.",null);
            else respond(action,false,"Unsupported action.",null);
        }catch(Exception e){respond(action,false,e.getMessage(),null);}
    }
    private void accountResponse(String action,FirebaseUser user){
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot->{
            JSONObject data=new JSONObject();JSONObject account=new JSONObject();
            try{
                account.put("uid",user.getUid());account.put("email",user.getEmail());
                account.put("username",user.getEmail());
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
