package com.roy.util.coderecognizer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2016/7/21.
 */
public class Camera2Controller implements CameraInterface{
    //static
    private static Camera2Controller cameraController;
    public final static int MAIN_CAMERA = 0;
    public final static int SECOND_CAMERA = 1;
    //context
    private Context mContext;
    //camera
    private SurfaceView mSurfaceView;
    private Camera mCamera;


    private Camera2Controller(SurfaceView sv, Context context) {
        super();
        mSurfaceView = sv;
        mContext = context;
    }
    public synchronized static CameraInterface getInstance(SurfaceView sv,Context context){
        if(cameraController==null){
            cameraController = new Camera2Controller(sv,context);
        }
        return cameraController;
    }

    @Override
    public void startPreview() {

    }

    @Override
    public void capturePicture() {

    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void captureVideo() {

    }

    @Override
    public void stopVideo() {

    }

    @Override
    public void openCamera(int type) {
        int number = getCameraNumber();
        int cameraType = 0;
        if(number>=2){
            if(type==SECOND_CAMERA){
                cameraType = type;
            }
        }
        mCamera = Camera.open(cameraType);
    }

    @Override
    public void closeCamera() {

    }

    @Override
    public boolean isSupportCamera() {
        if(mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int getCameraNumber() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public int getCameraState() {
        return 0;
    }

    @Override
    public void setAutoFocus() {

    }
}
