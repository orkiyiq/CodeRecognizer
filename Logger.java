package com.roy.util.coderecognizer;

import android.util.Log;

/**
 * Created by Administrator on 2016/7/25.
 */
public class Logger {
    public static void debug(boolean isEnable,String tag,String description){
        if(isEnable){
            Log.v("ROY: "+tag, description);
        }
    }
    public static void traceException(boolean isEnable,String tag,String description,Exception e){
        if(isEnable){
            Log.v("ROY: "+tag, description+Log.getStackTraceString(e));
        }
    }
}
