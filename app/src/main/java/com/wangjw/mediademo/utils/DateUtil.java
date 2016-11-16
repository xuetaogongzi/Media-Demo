package com.wangjw.mediademo.utils;

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

}
