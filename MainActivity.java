package com.roy.util.coderecognizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener{

    //logger switch
    private boolean isLogger = true;
    private final String TAG = this.getClass().toString();
    //const
    public final static int CAPTURE = 0;
    public final static int RECOGNIZE = 1;
    public final static int RECORD = 2;
    private final static String ImageFile_Prefix = "Image_";
    private final static String ImageFile_Suffix = ".jpg";
    private final static String VideoFile_Prefix = "Video_";
    private final static String VideoFile_Suffix = ".mp4";
    public final static int MEDIA_CAPTURE = 0;
    public final static int MEDIA_VIDEO = 1;
    private final static int PICK_PIC = 10;
    private static final int QR_WIDTH = 200;
    private static final int QR_HEIGHT = 200;
    private static final int CODE128_WIDTH = 200;
    private static final int CODE128_HEIGHT = 80;
    //ui
    private ImageView mImageView_Captured;
    private Button mButton_capture,mButton_recognize,mButton_record,mButton_recognize_local,mButton_create;
    private EditText mEditText_input;
    private Spinner mSpinner_codeFormat;
    //spinner data
    private BarcodeFormat[] codefromatArray;
    private ArrayAdapter<BarcodeFormat> mAdapter;
    private BarcodeFormat code_format = BarcodeFormat.QR_CODE;
    //capture file
    private File mFile_Capture;
    private String imagefile_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();

    }
    //initial
    private void initial(){
        //spinner data
        codefromatArray = BarcodeFormat.values();
        mAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,codefromatArray);
        //检测摄像
        if(!Util.isSupportCamera(this)){
            Toast.makeText(this,"设备不支持摄像头",Toast.LENGTH_SHORT).show();
            finish();
        }

        mImageView_Captured = (ImageView)findViewById(R.id.image_captured);
        mButton_capture = (Button)findViewById(R.id.button_capture);
        mButton_recognize = (Button)findViewById(R.id.button_recognize);
        mButton_record = (Button)findViewById(R.id.button_record);
        mButton_recognize_local = (Button)findViewById(R.id.button_recognize_local);
        mButton_create = (Button)findViewById(R.id.button_create);
        mEditText_input = (EditText)findViewById(R.id.content_input);
        mSpinner_codeFormat = (Spinner)findViewById(R.id.spinner_codeFormat);
        mSpinner_codeFormat.setAdapter(mAdapter);
        mSpinner_codeFormat.setOnItemSelectedListener(this);
        //bind listener
        mButton_recognize.setOnClickListener(this);
        mButton_capture.setOnClickListener(this);
        mButton_record.setOnClickListener(this);
        mButton_create.setOnClickListener(this);
        mButton_recognize_local.setOnClickListener(this);
        initialViewLocation(mImageView_Captured);
        //data
        imagefile_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();

    }
    //initialImageView
    private void initialViewLocation(View v){
        DisplayInfo displayInfo = Util.getDisplayInfo(this);
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        int w =(int) (displayInfo.w*0.8);
        int h = (int)(displayInfo.h*0.4);
        layoutParams.width = w;
        layoutParams.height = h;
        v.setLayoutParams(layoutParams);
//        v.getParent().requestLayout();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_capture:
                startCapture();
                break;
            case R.id.button_recognize:
                startRecognize();
                break;
            case R.id.button_record:
                startRecord();
                break;
            case R.id.button_recognize_local:
                pickPic();
                break;
            case R.id.button_create:
                String content = mEditText_input.getText().toString();
                if(content.equals("")){
                    Toast.makeText(this,"请输入条码信息",Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.v("roytest", "code format is " + code_format);
                if(!ZXUtil.is2DCoding(code_format)){
                    if(!ZXUtil.isNumberAndCharInput(content)){
                        Toast.makeText(this,"该条码格式只支持数字和英文字母",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                int w,h;
                if(code_format == BarcodeFormat.QR_CODE||code_format==BarcodeFormat.AZTEC||code_format==BarcodeFormat.DATA_MATRIX){
                    w = QR_WIDTH;
                    h = QR_HEIGHT;
                }else{
                    w = CODE128_WIDTH;
                    h = CODE128_HEIGHT;
                }
                BitMatrix bitMatrix = startCreateCode(content, w, h, code_format);
//                File file = ZXUtil.writeCodeToFile(bitMatrix, null);
                Bitmap bitmaplogo = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
                File file = ZXUtil.writeCodeToFileWithLog(bitMatrix,null,bitmaplogo);
                if(file==null){
                    Toast.makeText(this,"创建条码图片失败",Toast.LENGTH_SHORT).show();
                    return;
                }
                Util.insertImageToMediaStore(this, null, file.getPath(), null, null);
                Bitmap bitmap = Util.getBitmapFromFile(file, mImageView_Captured);
                if(bitmap!=null){
                    mImageView_Captured.setImageBitmap(bitmap);
                }
                break;
        }
    }
    //pick picture
    private void pickPic(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_PIC);
    }

    private BitMatrix startCreateCode(String content,int w,int h,BarcodeFormat format) {
        BitMatrix bitMatrix = ZXUtil.createQrcode(content, w, h, format);
        return bitMatrix;
    }

    private String startRecognizeLocal(String filepath) {
        Bitmap bitmap = Util.getBitmapFromFile(new File(filepath), null);
        if(bitmap == null){
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        String result = ZXUtil.decodeZXingRGB(pixels, w, h);
        return result;
    }

    private void startRecognize() {
        Intent intent = new Intent(MainActivity.this,CameraActivity.class);
        startActivityForResult(intent, RECOGNIZE);
    }

    private void startRecord(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri uri = getUri(MEDIA_VIDEO);
        if(uri!=null){
            intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        }
        startActivityForResult(intent,RECORD);
    }
    private void startCapture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = getUri(MEDIA_CAPTURE);
        if(uri!=null){
            intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        }
        startActivityForResult(intent, CAPTURE);
    }
    //get uri
    private Uri getUri(int type){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String date = simpleDateFormat.format(new Date());
        Uri uri = null;
        switch (type){
            case MEDIA_CAPTURE:
                mFile_Capture = new File(imagefile_path+File.separator+ImageFile_Prefix+date+ImageFile_Suffix);
                break;
            case MEDIA_VIDEO:
                mFile_Capture = new File(imagefile_path+File.separator+VideoFile_Prefix+date+VideoFile_Suffix);
                break;
        }
        try {
            if(mFile_Capture.exists()){
                mFile_Capture.delete();
                mFile_Capture.createNewFile();
            }
        }catch (IOException e) {
            Logger.traceException(isLogger,TAG,"IO Exception:\n",e);
        }
        uri = Uri.fromFile(mFile_Capture);
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CAPTURE:
                handleCaptureResult(resultCode,data);
                break;
            case RECOGNIZE:
                handleRecognizeResult(resultCode,data);
                break;
            case PICK_PIC:
                handlePickResult(resultCode,data);
        }
    }

    private void handlePickResult(int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            if(imageUri==null){
                return;
            }
            startRecognizeLocal(Util.getFilePathFromUri(this,imageUri));
        }
    }

    //
    private void handleCaptureResult(int resultCode,Intent data){
        Bitmap b = null;
        if(resultCode == RESULT_OK){
            if(data==null){
                b = Util.getBitmapFromFile(mFile_Capture, mImageView_Captured);
                if(b == null){
                    return;
                }

            }else{
                b = data.getParcelableExtra("data");
                if(b == null){
                    return;
                }
            }
            mImageView_Captured.setImageBitmap(b);
        }else{
            Logger.debug(isLogger,TAG,"capture cancel");
        }
    }
    private void handleRecognizeResult(int resultCode,Intent data){
        if(resultCode == RESULT_OK){
            Bitmap bitmap = data.getParcelableExtra("bitmap");
            if(bitmap!=null){
                mImageView_Captured.setImageBitmap(bitmap);
            }
        }else{
            Logger.debug(isLogger, TAG, "recognize cancel");
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        BarcodeFormat barcodeFormat = (BarcodeFormat)parent.getItemAtPosition(position);
        code_format = barcodeFormat;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
