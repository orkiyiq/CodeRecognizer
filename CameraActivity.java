package com.roy.util.coderecognizer;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraActivity extends AppCompatActivity {
    //const
    public final static int CAPTURE_COMPLETED = 0;
    //ui
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    //camera2
    private CameraManager mCameraManager;
    private CaptureRequest.Builder mBuilder_Preview = null;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private static Handler mHandler = null;
    private ImageReader mImageReader;
    private String cameraId;
    private Bitmap mBitmap_CaptureForRecognize;
//    private MyHandler<CameraActivity> mMyHandler;
    //camera
    private CameraInterface mCameraInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initial();
    }

    //initial
    private void initial() {

        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mCallback_surfaceHolder);
        //camera

        initialCamera();
/*        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            setResult(RESULT_CANCELED);
            finish();
        }*/
        //handler
//        mMyHandler = new MyHandler(this);


    }
    //initial camera
    private void initialCamera(){
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP){
            mCameraInterface = Camera2Controller.getInstance(mSurfaceView,this);
        }else{
            mCameraInterface = CameraController.getInstance(mSurfaceView,this);
        }
        if(!mCameraInterface.isSupportCamera()){
            finish();
        }
    }

    //callback
    SurfaceHolder.Callback mCallback_surfaceHolder = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
//            initialCameraPreview();
            mCameraInterface.openCamera(CameraInterface.MAIN_CAMERA);
            Log.v("roytest","surface created");
        }

/*
        private void initialCameraPreview() {
*/
/*            HandlerThread handlerThread = new HandlerThread("CameraPreview");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());*//*


            mImageReader = ImageReader.newInstance(mSurfaceView.getWidth(), mSurfaceView.getHeight(), ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {

                }
            }, mHandler);

            try {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String[] cameraidlist = mCameraManager.getCameraIdList();
                if(cameraidlist.length>0){
                    for(String i:cameraidlist){
                        Log.v("roytest","camera id "+i);
                    }
                    cameraId = cameraidlist[0];
                }
                mCameraManager.openCamera(cameraId, mStateCallback_cameraDevice, mHandler);
            } catch (CameraAccessException e) {
                Log.v("roytest","access exception :\n"+Log.getStackTraceString(e));
            }
        }
*/

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mCameraInterface.startPreview();
            Log.v("roytest", "surface changed");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCameraInterface.stopPreview();
            Log.v("roytest", "surface destroyed");
        }
    };
/*    CameraDevice.StateCallback mStateCallback_cameraDevice = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            createCameraCaptureSession();
            Log.v("roytest","on opened");

        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.v("roytest","on disconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
        //
        private void createCameraCaptureSession(){

            try {
                mBuilder_Preview = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mBuilder_Preview.addTarget(mSurfaceHolder.getSurface());
                mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(),mImageReader.getSurface()),mStateCallback_captureSession,mHandler);
            } catch (CameraAccessException e) {
                Log.v("roytest", "access exception :\n" + Log.getStackTraceString(e));
            }

        }

        @Override
        public void onClosed(CameraDevice camera) {
            super.onClosed(camera);
            Log.v("roytest","on closed");
        }
    };
    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.v("roytset","on capture started");
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.v("roytset", "on capture progressed");
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
                if(result.get(CaptureResult.CONTROL_AF_STATE)==CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED) {

                    mBuilder_Preview.set(CaptureRequest.CONTROL_AF_TRIGGER,CaptureRequest.CONTROL_AF_TRIGGER_START);
                    mBuilder_Preview.addTarget(mImageReader.getSurface());
                    try {
                        session.setRepeatingRequest(mBuilder_Preview.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                                Image image = mImageReader.acquireLatestImage();
                                if(image!=null){
                                    try {
                                        session.stopRepeating();
                                        Image.Plane[] planes = image.getPlanes();
                                        ByteBuffer byteBuffer = planes[0].getBuffer();
                                        byte[] bytes = new byte[byteBuffer.remaining()];
                                        byteBuffer.get(bytes);
                                        mBitmap_CaptureForRecognize = Util.getBitmapFromBytesArray(bytes,null);
                                        Message message = mMyHandler.obtainMessage();
                                        message.what = CAPTURE_COMPLETED;
                                        mMyHandler.sendMessage(message);

                                    } catch (CameraAccessException e) {
                                        Log.v("roytest", "access exception :\n" + Log.getStackTraceString(e));
                                    }finally {
//                                        mCameraDevice.close();
                                    }
                                }
                            }
                        },mHandler);

                    } catch (CameraAccessException e) {
                        Log.v("roytest", "access exception :\n" + Log.getStackTraceString(e));
                    }
                    Log.v("roytest","on capture completed");
                }



        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.v("roytset", "on capture failed");
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.v("roytset", "on capture sequence completed");
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.v("roytset", "on capture sequence aborted");
        }
    };
    CameraCaptureSession.StateCallback mStateCallback_captureSession = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mCameraCaptureSession = session;
            mBuilder_Preview.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            try {
                session.setRepeatingRequest(mBuilder_Preview.build(),mCaptureCallback,mHandler);
            } catch (CameraAccessException e) {
                Log.v("roytest", "access exception :\n" + Log.getStackTraceString(e));
            }
            Log.v("roytest","on configured");
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };*/
/*    private static class MyHandler<T extends CameraActivity> extends Handler{
        WeakReference<Activity> mActivityWeakReference;
        T mActivity;

        public MyHandler(Activity activity) {
            super();
            mActivityWeakReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mActivityWeakReference!=null){
                mActivity =(T) mActivityWeakReference.get();
            }
            if(mActivity!=null){
                switch (msg.what){
                    case CameraInterface.CAMERA_READY:
                        break;
                }
            }
        }
    }
    //returnResult
    protected void sendResult(){
        Intent intent = new Intent(CameraActivity.this,MainActivity.class);
        intent.putExtra("bitmap", mBitmap_CaptureForRecognize);
        setResult(RESULT_OK, intent);
        mCameraDevice.close();
        this.finish();
    }*/

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
/*
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            mCameraInterface.setAutoFocus();
        }
*/

        return super.onTouchEvent(event);
    }
}
