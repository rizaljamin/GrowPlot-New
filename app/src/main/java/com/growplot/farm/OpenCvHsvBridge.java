package com.growplot.farm;
import android.graphics.Bitmap;import android.util.Base64;import android.webkit.JavascriptInterface;
import org.json.JSONObject;import org.opencv.android.OpenCVLoader;import org.opencv.android.Utils;import org.opencv.core.Core;import org.opencv.core.CvType;import org.opencv.core.Mat;import org.opencv.core.MatOfPoint;import org.opencv.core.Point;import org.opencv.core.Rect;import org.opencv.core.Scalar;import org.opencv.core.Size;import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayOutputStream;import java.util.ArrayList;import java.util.List;import java.util.Locale;
/** Native OpenCV HSV transform, estimated plant boundary and colour pixel counts. */
public final class OpenCvHsvBridge{
 private final boolean ready;public OpenCvHsvBridge(){boolean ok;try{ok=OpenCVLoader.initLocal();}catch(Throwable e){ok=false;}ready=ok;}
 @JavascriptInterface public String analyzeImage(String input){
  Mat rgba=new Mat(),rgb=new Mat(),hsv=new Mat(),gm=new Mat(),ym=new Mat(),bm=new Mat(),core=new Mat(),plantMask=new Mat(),preview=new Mat(),boundary=new Mat(),mask=new Mat(),hierarchy=new Mat(),contourInput=new Mat(),closeKernel=new Mat(),openKernel=new Mat(),edgeKernel=new Mat();
  List<MatOfPoint> contours=new ArrayList<>(),selected=new ArrayList<>();
  try{
   if(!ready)return error("OpenCV could not initialize in this APK.");if(input==null||input.isEmpty())return error("The photo data was empty.");
   String data=input.startsWith("data:")?input.substring(input.indexOf(',')+1):input;byte[] bytes=Base64.decode(data,Base64.DEFAULT);
   Bitmap bitmap=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length);if(bitmap==null)return error("OpenCV could not decode the selected photo.");
   Utils.bitmapToMat(bitmap,rgba);bitmap.recycle();Imgproc.cvtColor(rgba,rgb,Imgproc.COLOR_RGBA2RGB);Imgproc.cvtColor(rgb,hsv,Imgproc.COLOR_RGB2HSV);
   Core.inRange(hsv,new Scalar(29,46,41),new Scalar(85,255,255),gm);Core.inRange(hsv,new Scalar(17,46,77),new Scalar(28,255,255),ym);Core.inRange(hsv,new Scalar(4,46,0),new Scalar(16,255,173),bm);
   // Restore Build 09 HSV thresholds and grouping, which produced the closer foliage outline.
   Core.inRange(hsv,new Scalar(17,35,30),new Scalar(100,255,255),core);
   closeKernel=Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(9,9));openKernel=Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(3,3));edgeKernel=Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(3,3));
   Imgproc.morphologyEx(core,core,Imgproc.MORPH_CLOSE,closeKernel);Imgproc.morphologyEx(core,core,Imgproc.MORPH_OPEN,openKernel);
   contourInput=core.clone();Imgproc.findContours(contourInput,contours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
   int width=hsv.cols(),height=hsv.rows();double largest=0;int anchor=-1;Point anchorCenter=null;
   for(int i=0;i<contours.size();i++){double area=Imgproc.contourArea(contours.get(i));Rect box=Imgproc.boundingRect(contours.get(i));double cx=box.x+box.width/2.0,cy=box.y+box.height/2.0;if(cx>=width*.08&&cx<=width*.92&&cy>=height*.04&&cy<=height*.96&&area>largest){largest=area;anchor=i;anchorCenter=new Point(cx,cy);}}
   if(anchor<0||largest<Math.max(20,(long)width*height*.00025))return error("No clear plant-like green/yellow cluster was found. Retake the photo with the plant larger and more central.");
   double linkDistance=Math.max(24,Math.min(width,height)*.60),minimumArea=Math.max(12,largest*.002);
   for(int i=0;i<contours.size();i++){MatOfPoint contour=contours.get(i);double area=Imgproc.contourArea(contour);Rect box=Imgproc.boundingRect(contour);double cx=box.x+box.width/2.0,cy=box.y+box.height/2.0;if(area>=minimumArea&&Math.hypot(cx-anchorCenter.x,cy-anchorCenter.y)<=linkDistance)selected.add(contour);}
   if(selected.isEmpty())return error("The plant boundary could not be formed from the detected foliage regions.");
   plantMask.create(height,width,CvType.CV_8UC1);plantMask.setTo(new Scalar(0));Imgproc.drawContours(plantMask,selected,-1,new Scalar(255),Imgproc.FILLED);Imgproc.dilate(plantMask,plantMask,edgeKernel);
   long photoPixels=(long)width*height,plantPixels=Core.countNonZero(plantMask);
   if(plantPixels<Math.max(30,photoPixels*.003))return error("The detected plant region is too small to measure reliably. Retake the photo with the plant closer and in focus.");
   Core.bitwise_and(gm,plantMask,gm);Core.bitwise_and(ym,plantMask,ym);Core.bitwise_and(bm,plantMask,bm);
   long green=Core.countNonZero(gm),yellow=Core.countNonZero(ym),brown=Core.countNonZero(bm),other=Math.max(0,plantPixels-green-yellow-brown);
   Imgproc.cvtColor(hsv,preview,Imgproc.COLOR_HSV2RGB);boundary=preview.clone();Imgproc.drawContours(boundary,selected,-1,new Scalar(255,0,255),2);
   mask.create(height,width,CvType.CV_8UC3);mask.setTo(new Scalar(0,0,0));mask.setTo(new Scalar(34,205,94),gm);mask.setTo(new Scalar(255,213,55),ym);mask.setTo(new Scalar(169,103,57),bm);
   JSONObject r=new JSONObject();r.put("ok",true);r.put("width",width);r.put("height",height);r.put("totalPixels",photoPixels);r.put("plantPixels",plantPixels);r.put("plantPercentOfPhoto",pct(plantPixels,photoPixels));r.put("greenPixels",green);r.put("yellowPixels",yellow);r.put("brownPixels",brown);r.put("otherPixels",other);r.put("greenPercent",pct(green,plantPixels));r.put("yellowPercent",pct(yellow,plantPixels));r.put("brownPercent",pct(brown,plantPixels));r.put("otherPercent",pct(other,plantPixels));r.put("boundaryMethod","HSV foliage-colour cluster estimate; inspect the outlined region");r.put("preview",png(preview));r.put("boundary",png(boundary));r.put("mask",png(mask));return r.toString();
  }catch(Throwable e){return error(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}
  finally{rgba.release();rgb.release();hsv.release();gm.release();ym.release();bm.release();core.release();plantMask.release();preview.release();boundary.release();mask.release();hierarchy.release();contourInput.release();closeKernel.release();openKernel.release();edgeKernel.release();for(MatOfPoint c:contours)c.release();}
 }
 private static String pct(long n,long total){return total==0?"0.0":String.format(Locale.US,"%.1f",n*100.0/total);}
 private static String png(Mat m)throws Exception{Bitmap b=Bitmap.createBitmap(m.cols(),m.rows(),Bitmap.Config.ARGB_8888);Utils.matToBitmap(m,b);ByteArrayOutputStream o=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,o);b.recycle();return Base64.encodeToString(o.toByteArray(),Base64.NO_WRAP);}
 private static String error(String m){JSONObject r=new JSONObject();try{r.put("ok",false);r.put("error",m);}catch(Exception ignored){}return r.toString();}
}