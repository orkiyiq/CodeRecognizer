package com.roy.util.coderecognizer;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2016/7/20.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private CameraManager mCameraManager;
    private SurfaceHolder mSurfaceHolder;
    public CameraPreview(Context context,CameraManager cameraManager) {
        super(context);
        mCameraManager = cameraManager;
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initialCameraPreview();
    }

    private void initialCameraPreview() {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
