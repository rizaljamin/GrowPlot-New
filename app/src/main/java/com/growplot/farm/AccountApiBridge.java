package com.growplot.farm;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Native HTTPS transport; never stores passwords or account tokens on disk. */
public final class AccountApiBridge {
    private final Activity activity;
    private final WebView web;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private volatile String token;
    public AccountApiBridge(Activity activity,WebView web){this.activity=activity;this.web=web;}
    @JavascriptInterface public boolean configured(){return BuildConfig.ACCOUNT_API_URL.startsWith("https://");}
    @JavascriptInterface public void request(String action,String payload){
        if(!configured()){respond(action,false,"Account server is not configured.",null);return;}
        if(!("login".equals(action)||"register".equals(action)||"profile".equals(action)||"saveProfile".equals(action)||"stats".equals(action))){
            respond(action,false,"Unsupported action",null);return;
        }
        executor.execute(()->{
            try{
                String path;String method="GET";
                switch(action){
                    case "login":path="/v1/login";method="POST";break;
                    case "register":path="/v1/register";method="POST";break;
                    case "profile":path="/v1/me";break;
                    case "saveProfile":path="/v1/me";method="PUT";break;
                    default:path="/v1/admin/stats";
                }
                URL url=new URL(BuildConfig.ACCOUNT_API_URL.replaceAll("/+$","")+path);
                if(!"https".equals(url.getProtocol()))throw new IOException("HTTPS required");
                HttpURLConnection c=(HttpURLConnection)url.openConnection();
                c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setRequestMethod(method);
                c.setRequestProperty("Accept","application/json");
                if(token!=null)c.setRequestProperty("Authorization","Bearer "+token);
                if("POST".equals(method)||"PUT".equals(method)){
                    c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");
                    byte[] bytes=payload.getBytes(StandardCharsets.UTF_8);
                    try(OutputStream out=c.getOutputStream()){out.write(bytes);}
                }
                int status=c.getResponseCode();
                InputStream stream=status>=400?c.getErrorStream():c.getInputStream();
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                if(stream!=null)try(InputStream in=stream){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);}
                JSONObject response=new JSONObject(bytes.toString("UTF-8"));
                if(status>=200&&status<300){
                    if(("login".equals(action)||"register".equals(action))&&response.has("token"))token=response.getString("token");
                    respond(action,true,"",response);
                }else respond(action,false,response.optString("error","Request failed ("+status+")"),null);
                c.disconnect();
            }catch(Exception e){respond(action,false,"Cannot reach account server: "+e.getClass().getSimpleName(),null);}
        });
    }
    @JavascriptInterface public void logout(){token=null;}
    private void respond(String action,boolean ok,String error,JSONObject data){
        activity.runOnUiThread(()->{
            try{
                JSONObject envelope=new JSONObject();envelope.put("action",action);envelope.put("ok",ok);
                envelope.put("error",error);envelope.put("data",data==null?JSONObject.NULL:data);
                web.evaluateJavascript("window.GrowPlotAccountResponse&&window.GrowPlotAccountResponse("+envelope.toString()+")",null);
            }catch(Exception ignored){}
        });
    }
}
