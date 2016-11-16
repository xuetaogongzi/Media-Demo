package com.wangjw.mediademo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by hjy on 3/4/16.<br>
 */
public class ToastUtil {

    private static Toast mToast;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private static Runnable mPendingDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if(mToast != null) {
                mToast.cancel();
                mToast = null;
            }
        }
    };

    public static void showToast(Context context, CharSequence text) {
        showToast(context, text, 2500);
    }

    public static void showToast(Context context, int textResId) {
        showToast(context, textResId, 2500);
    }

    /**
     *
     * @param context
     * @param text
     * @param duration 持续时间, 毫秒
     */
    public static void showToast(Context context, CharSequence text, int duration) {
        if(context == null)
            return;
        mHandler.removeCallbacks(mPendingDismissRunnable);
        if(mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mHandler.postDelayed(mPendingDismissRunnable, duration);
        mToast.show();
    }

    /**
     *
     * @param context
     * @param textResId
     * @param duration 持续时间, 毫秒
     */
    public static void showToast(Context context, int textResId, int duration) {
        if(context == null)
            return;
        showToast(context, context.getString(textResId), duration);
    }

}