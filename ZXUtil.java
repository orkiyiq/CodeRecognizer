package com.roy.util.coderecognizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/7/21.
 */
public class ZXUtil {
    //reg
    public final static String PATTER_NUMBERANDCHAR = "[0-9a-zA-Z]+";
    //CONST
    private final static int COLOR_BLACK = 0xff000000;
    private final static int COLOR_WHITE = 0xffffffff;
    public static String decodeZXing(byte[] data){
        Result decode_result = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0,data.length);
        LuminanceSource luminanceSource = new PlanarYUVLuminanceSource(data,options.outWidth,options.outHeight,0,0,options.outWidth,options.outHeight,false);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        Map<DecodeHintType,String> hints = new HashMap<>();
//        hints.put(DecodeHintType.CHARACTER_SET,"UTF-8");
        try {
            decode_result = multiFormatReader.decode(binaryBitmap);
        } catch (NotFoundException e) {
            Log.v("roytest","decode fail:\n"+Log.getStackTraceString(e));
        }
        if(decode_result!=null){
            return decode_result.getText();
        }
        return null;
    }

    public static String decodeZXingRGB(int[] data,int w,int h){
        Result decode_result = null;
        LuminanceSource l = new RGBLuminanceSource(w,h,data);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(l));
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        Map<DecodeHintType,String> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET,"UTF-8");
        try {
            decode_result = multiFormatReader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            Log.v("roytest","decode fail:\n"+Log.getStackTraceString(e));
        }
        if(decode_result!=null){
            return decode_result.getText();
        }
        return null;
    }
    public static BitMatrix createQrcode(String content,int w,int h,BarcodeFormat format){
        String fileFormat = "png";
        BitMatrix bitMatrix = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType,String> hints = new HashMap<>();
        if(format == BarcodeFormat.DATA_MATRIX){
            hints.put(EncodeHintType.CHARACTER_SET,"ISO-8859-1");
        }else{
            hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
        }

        try {
            bitMatrix = multiFormatWriter.encode(content, format, w, h, hints);
//            Log.v("roytest","bitMatrix is \n"+bitMatrix.toString());
        } catch (WriterException e) {
            Log.v("roytest", "encode fail:\n" + Log.getStackTraceString(e));
        }
        return bitMatrix;
    }
    public static File writeCodeToFile(BitMatrix bitMatrix,File file){
        if(bitMatrix == null){
            Log.v("roytest","bitmatrix null");
            return null;
        }

        int[] pixels = getPixelsArray(bitMatrix);
        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        Bitmap bitmap = convertArrayToBitmap(pixels, Bitmap.Config.ARGB_8888,w,h);
        if(bitmap==null){
            Log.v("roytest", "bitmmap null");
            return null;
        }

        return Util.createFileFromBitmap(bitmap,file);

    }
    //
    public static File writeCodeToFileWithLog(BitMatrix bitMatrix,File file,Bitmap logo){
        if(bitMatrix == null){
            Log.v("roytest","bitmatrix null");
            return null;
        }

        int[] pixels = getPixelsArray(bitMatrix);
        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        Bitmap bitmap = Util.addBitmap(convertArrayToBitmap(pixels, Bitmap.Config.ARGB_8888,w,h),logo,0.2f);

        if(bitmap==null){
            Log.v("roytest","bitmmap null");
            return null;
        }

        return Util.createFileFromBitmap(bitmap,file);

    }
    //
    public static int[] getPixelsArray(BitMatrix bitMatrix){
        if(bitMatrix==null){
            return null;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixelsARGB = new int[width*height];
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                if(bitMatrix.get(i,j)){
                    pixelsARGB[j*width+i]=COLOR_BLACK;
                }else{
                    pixelsARGB[j*width+i]=COLOR_WHITE;
                }
            }
        }
        return pixelsARGB;
    }
    //
    public static Bitmap convertArrayToBitmap(int[] pixels,Bitmap.Config format,int width,int height){
        Bitmap bitmap = null;
        if(pixels==null){
            return null;
        }
        try{
            bitmap = Bitmap.createBitmap(pixels,0,width,width,height, format);
        }catch (IllegalArgumentException e){
            Log.v("roytest","illegal argument error:\n"+Log.getStackTraceString(e));
        }
        return bitmap;
    }
    //
    public static boolean is2DCoding(BarcodeFormat format){
        if(format == BarcodeFormat.QR_CODE||format == BarcodeFormat.AZTEC ||format == BarcodeFormat.PDF_417 || format == BarcodeFormat.DATA_MATRIX){
            return true;
        }
        return false;
    }
    public static boolean isNumberAndCharInput(String input){
        Pattern p = Pattern.compile(PATTER_NUMBERANDCHAR);
        Matcher m = p.matcher(input);
        if(m.matches()){
            return true;
        }
        return false;
    }
}
