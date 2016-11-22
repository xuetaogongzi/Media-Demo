package com.wangjw.mediademo.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wangjw on 16/11/16.
 */

public class DateUtil {

    public static String formatAudioRecordTime(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return "0" + m + ":" + (s >= 10 ? "" + s : "0" + s);
    }

    public static String formatDurationAudioTime(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return m + "’" + s + "’’";
    }

    /**
     * 将long型格式转化为所需格式的日期，以字符串返回
     *
     * @param time
     *            需要转化的时间，单位是毫秒
     * @param pattern
     *            待转化的格式
     * @return
     */
    public static String format(long time, String pattern) {
        if (time < 0 || TextUtils.isEmpty(pattern)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
        java.util.Date dt = new Date(time);
        return sdf.format(dt);
    }

}
