package com.roy.util.coderecognizer;

/**
 * Created by Administrator on 2016/7/21.
 */
public interface CameraInterface {
    int MAIN_CAMERA = 0;
    int SECOND_CAMERA = 1;
    int CAMERA_READY = 10;
    int CAMERA_CLOSED = 11;
    int CAMERA_OPENNING = 12;
    int CAMERA_CLOSING = 13;
    int CAMERA_UNKNOWN = 14;
    void startPreview();
    void capturePicture();
    void stopPreview();
    void captureVideo();
    void stopVideo();
    void openCamera(int type);
    void closeCamera();
    boolean isSupportCamera();
    int getCameraNumber();
    int getCameraState();
    void setAutoFocus();
}
