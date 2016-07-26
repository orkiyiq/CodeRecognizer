package com.roy.util.coderecognizer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Administrator on 2016/7/21.
 */
public class CameraController implements CameraInterface{
    //const
    private final static long DURATION = 100;
    //static
    private static CameraController cameraController;

    //context
    private static Context mContext;
    //camera
    private static SurfaceView mSurfaceView;
    private Camera mCamera;
    //stat flag
    private int stateFlag = CAMERA_UNKNOWN;
    private boolean isFoucing = false;


    private CameraController() {
        super();

    }
    public synchronized static CameraInterface getInstance(SurfaceView sv,Context context){
        if(cameraController==null){
            cameraController = new CameraController();
        }
        mSurfaceView = sv;
        mContext = context;
        return cameraController;
    }

    @Override
    public void startPreview() {
        try {
            while(stateFlag!=CAMERA_READY){
                if(stateFlag==CAMERA_UNKNOWN){
                    return;
                }
                Thread.sleep(DURATION);
            }
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();
            stateFlag = CAMERA_OPENNING;
            setAutoFocus();
        } catch (IOException e) {
            Log.v("roytest","start preview failed:\n"+Log.getStackTraceString(e));
            mCamera.release();
            stateFlag = CAMERA_UNKNOWN;
        } catch (InterruptedException e) {
            Log.v("roytest", "sleep failed:\n" + Log.getStackTraceString(e));
            mCamera.release();
            stateFlag = CAMERA_UNKNOWN;
        }
    }

    @Override
    public void capturePicture() {
        mCamera.takePicture(null,null,mPictureCallback);
        Log.v("roytest","capture picture");
    }

    @Override
    public void stopPreview() {
        closeCamera();
    }

    @Override
    public void captureVideo() {

    }

    @Override
    public void stopVideo() {

    }

    @Override
    public synchronized void openCamera(int type) {
        int number = getCameraNumber();
        int cameraType = 0;
        if(number>=2){
            if(type==SECOND_CAMERA){
                cameraType = type;
            }
        }
        if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Log.v("roytest","用户取消授权");
            return;
        }
        final int finalCameraType = cameraType;
        stateFlag = CAMERA_OPENNING;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    mCamera = Camera.open(finalCameraType);
                    stateFlag = CAMERA_READY;
                    Log.v("roytest","camera opened");
                }catch (RuntimeException e){
                    Log.v("roytest","camera open failed:" + Log.getStackTraceString(e));
                    stateFlag = CAMERA_UNKNOWN;
//                    throw new RuntimeException("camera open failed");
                }
            }
        });
        thread.start();
    }

    @Override
    public void closeCamera() {
        if(mCamera!=null){
            stateFlag = CAMERA_CLOSING;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        mCamera.stopPreview();
                        mCamera.release();
                        stateFlag = CAMERA_CLOSED;
                        Log.v("roytest","camera closed");
                    }catch (RuntimeException e){
                        Log.v("roytest","camera open failed:" + Log.getStackTraceString(e));
                        stateFlag = CAMERA_UNKNOWN;
//                    throw new RuntimeException("camera open failed");
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    public boolean isSupportCamera() {
        if(mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            Toast.makeText(mContext,"设备不支持摄像头",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public int getCameraNumber() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public int getCameraState() {
        return stateFlag;
    }

    @Override
    public synchronized void setAutoFocus() {
        Log.v("roytest", "current focus mode is " + mCamera.getParameters().getFocusMode());
        isFoucing = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isFoucing){
                    mCamera.autoFocus(mAutoFocusCallback);
                    Log.v("roytest","set AF");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.v("roytest", "camera focus failed:" + Log.getStackTraceString(e));
                    }
                }
                mCamera.cancelAutoFocus();
                Log.v("roytest","finish set AF thread");
            }
        });
        thread.start();
    }
    //
    private void cancelAutoFocus(){
        isFoucing = false;
    }

    protected Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if(success){
                Log.v("roytest","on auto focus success");
                capturePicture();
                cancelAutoFocus();
            }else{
                Log.v("roytest","on auto focus fail");
            }
        }
    };
    protected Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.v("roytest","data size is " + data.length);
/*            ImageView imageView = new ImageView(mContext);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(100,50,20,20,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                    PixelFormat.TRANSPARENT);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Bitmap bitmap = Util.getBitmapFromBytesArray(data, null);
            if(bitmap!=null){
                WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
                windowManager.addView(imageView,layoutParams);
                imageView.setImageBitmap(bitmap);
                Log.v("roytest","set bitmap");
            }*/

/*            String result = ZXUtil.decodeZXing(data);*/

        }
    };
}
