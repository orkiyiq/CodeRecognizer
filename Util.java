package com.roy.util.coderecognizer;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 2016/7/20.
 */
public class Util {

    //屏幕尺寸
    public static DisplayInfo getDisplayInfo(Context context){
        DisplayInfo displayInfo = null;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        float d = displayMetrics.density;
        displayInfo = new DisplayInfo(w,h,d);
        return displayInfo;
    }
    //Camera检测
    public static boolean isSupportCamera(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }
    //Camera数量
    //bitmap
    //get bitmap from file
    public static Bitmap getBitmapFromFile(File file,View v){
        if(!file.exists()){
            return null;
        }
        Bitmap bitmap = null;
        String filepath = file.getPath();
        if(v != null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            int w = layoutParams.width;
            int h = layoutParams.height;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filepath, options);
            float xRatio = options.outWidth/(float)w;
            float yRatio = options.outHeight/(float)h;
            int sample =(int)Math.ceil(xRatio>yRatio?xRatio:yRatio);
            options.inSampleSize = sample;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filepath, options);
        }else{
            bitmap = BitmapFactory.decodeFile(filepath);
        }
        return bitmap;
    }
    //from bytes
    public static Bitmap getBitmapFromBytesArray(byte[] bytes,View v){
        if(bytes.length==0){
            return null;
        }
        Bitmap bitmap = null;

        if(v != null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            int w = layoutParams.width;
            int h = layoutParams.height;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            float xRatio = options.outWidth/(float)w;
            float yRatio = options.outHeight/(float)h;
            int sample =(int)Math.ceil(xRatio>yRatio?xRatio:yRatio);
            options.inSampleSize = sample;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }else{
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        return bitmap;
    }
    public static File createFileFromBitmap(Bitmap bitmap,File file){
        File imageFile = null;
        if(file==null){
            imageFile = createRandomPNGFile();
            if(imageFile==null){
                Log.v("roytest","imageFile null");
                return null;
            }
        }else {
            imageFile = file;
        }
        FileOutputStream fileOutputStream = null;
        if(bitmap==null || imageFile == null){
            Log.v("roytest","bitmmap&&imageFile null");
            return null;
        }
        try {
            fileOutputStream = new FileOutputStream(imageFile);
            boolean result = bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            if(!result){
                Log.v("roytest","bitmap compress result fail");
                return null;
            }
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            Log.v("roytest","file not found:\n"+Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.v("roytest", "io error:\n" + Log.getStackTraceString(e));
        }finally {
            if(fileOutputStream!=null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imageFile;
    }
    public static File createRandomPNGFile(){
        File file = null;
        int i = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String filename = simpleDateFormat.format(new Date());
        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+File.separator+filename+".png";
        file = new File(filepath);
        while(file.exists()){
            i++;
            filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+File.separator+filename+"("+i+")"+".png";
            file = new File(filepath);
        }
        return file;
    }
    public static String getFilePathFromUri(Context context,Uri uri){
        if(uri == null){
            return null;
        }
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context,uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public static void insertImageToMediaStore(Context context,Bitmap bitmap,String filepath,String title,String description){
        if(bitmap!=null){
            MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap,title,description);
        }else if(filepath!=null){
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),filepath,title,description);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("file://"+filepath)));
        }
    }
    //
    public static Bitmap addBitmap(Bitmap b1,Bitmap b2,float scale){
        float scaleRatio = 0;
        if(b1==null||b2==null){
            Log.v("roytest","b1,b2 null");
            return null;
        }
        int b1_w = b1.getWidth();
        int b1_h = b1.getHeight();
        int b2_w = b2.getWidth();
        int b2_h = b2.getHeight();

        if(b1_w == 0 || b1_h == 0){
            Log.v("roytest","b1 ");
            return null;
        }
        if(b2_w == 0||b2_h == 0){
            Log.v("roytest","b2 width height");
            return b1;
        }
        if(scale<0){
            scaleRatio = 1;
        }else{
            scaleRatio = scale;
        }

        Bitmap bitmap = Bitmap.createBitmap(b1_w,b1_h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(b1, 0, 0, null);
        canvas.save();
        canvas.scale(scaleRatio,scaleRatio,canvas.getWidth()/2,canvas.getHeight()/2);
/*        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(canvas.getWidth()/2,canvas.getHeight()/2,canvas.getWidth()/2,paint);*/
        canvas.drawBitmap(b2, 0, 0, null);
        canvas.restore();
        return bitmap;
    }
}
