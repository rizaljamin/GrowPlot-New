package com.growplot.farm;
import android.graphics.Bitmap;import android.util.Base64;import android.webkit.JavascriptInterface;
import org.json.JSONObject;import org.opencv.android.OpenCVLoader;import org.opencv.android.Utils;import org.opencv.core.Core;import org.opencv.core.CvType;import org.opencv.core.Mat;import org.opencv.core.Scalar;import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayOutputStream;import java.util.Locale;
/** Native OpenCV HSV transform and colour pixel counts for diagnosis section 1. */
public final class OpenCvHsvBridge{
 private final boolean ready;public OpenCvHsvBridge(){boolean ok;try{ok=OpenCVLoader.initLocal();}catch(Throwable e){ok=false;}ready=ok;}
 @JavascriptInterface public String analyzeImage(String input){
  Mat rgba=new Mat(),rgb=new Mat(),hsv=new Mat(),gm=new Mat(),ym=new Mat(),bm=new Mat(),preview=new Mat(),hue=new Mat(),sat=new Mat(),val=new Mat(),mask=new Mat();
  try{
   if(!ready)return error("OpenCV could not initialize in this APK.");if(input==null||input.isEmpty())return error("The photo data was empty.");
   String data=input.startsWith("data:")?input.substring(input.indexOf(',')+1):input;byte[] bytes=Base64.decode(data,Base64.DEFAULT);
   Bitmap bitmap=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length);if(bitmap==null)return error("OpenCV could not decode the selected photo.");
   Utils.bitmapToMat(bitmap,rgba);bitmap.recycle();Imgproc.cvtColor(rgba,rgb,Imgproc.COLOR_RGBA2RGB);Imgproc.cvtColor(rgb,hsv,Imgproc.COLOR_RGB2HSV);
   Core.inRange(hsv,new Scalar(29,46,41),new Scalar(85,255,255),gm);Core.inRange(hsv,new Scalar(17,46,77),new Scalar(28,255,255),ym);Core.inRange(hsv,new Scalar(4,46,0),new Scalar(16,255,173),bm);
   long total=(long)hsv.rows()*hsv.cols(),green=Core.countNonZero(gm),yellow=Core.countNonZero(ym),brown=Core.countNonZero(bm),other=Math.max(0,total-green-yellow-brown);
   Imgproc.cvtColor(hsv,preview,Imgproc.COLOR_HSV2RGB);Core.extractChannel(hsv,hue,0);Core.multiply(hue,new Scalar(255.0/179.0),hue);Core.extractChannel(hsv,sat,1);Core.extractChannel(hsv,val,2);
   mask=Mat.zeros(hsv.rows(),hsv.cols(),CvType.CV_8UC3);mask.setTo(new Scalar(34,205,94),gm);mask.setTo(new Scalar(255,213,55),ym);mask.setTo(new Scalar(169,103,57),bm);
   JSONObject r=new JSONObject();r.put("ok",true);r.put("width",hsv.cols());r.put("height",hsv.rows());r.put("totalPixels",total);r.put("greenPixels",green);r.put("yellowPixels",yellow);r.put("brownPixels",brown);r.put("otherPixels",other);
   r.put("greenPercent",pct(green,total));r.put("yellowPercent",pct(yellow,total));r.put("brownPercent",pct(brown,total));r.put("otherPercent",pct(other,total));r.put("preview",png(preview));r.put("hue",png(hue));r.put("saturation",png(sat));r.put("value",png(val));r.put("mask",png(mask));return r.toString();
  }catch(Throwable e){return error(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}
  finally{rgba.release();rgb.release();hsv.release();gm.release();ym.release();bm.release();preview.release();hue.release();sat.release();val.release();mask.release();}
 }
 private static String pct(long n,long total){return total==0?"0.0":String.format(Locale.US,"%.1f",n*100.0/total);}
 private static String png(Mat m)throws Exception{Bitmap b=Bitmap.createBitmap(m.cols(),m.rows(),Bitmap.Config.ARGB_8888);Utils.matToBitmap(m,b);ByteArrayOutputStream o=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,o);b.recycle();return Base64.encodeToString(o.toByteArray(),Base64.NO_WRAP);}
 private static String error(String m){JSONObject r=new JSONObject();try{r.put("ok",false);r.put("error",m);}catch(Exception ignored){}return r.toString();}
}