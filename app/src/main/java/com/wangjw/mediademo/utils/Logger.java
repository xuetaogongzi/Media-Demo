package com.wangjw.mediademo.utils;

import android.util.Log;

import com.wangjw.mediademo.BuildConfig;


/**
 * Created by hjy on 7/21/15.<br>
 */
public class Logger {

    private static boolean DEBUG = BuildConfig.DEBUG;

    public static void d(String tag, String msg) {
        if(DEBUG)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if(DEBUG)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if(DEBUG)
            Log.v(tag, msg);
    }

    public static void i(String tag, String msg) {
        if(DEBUG)
            Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if(DEBUG)
            Log.w(tag, msg);
    }

}
