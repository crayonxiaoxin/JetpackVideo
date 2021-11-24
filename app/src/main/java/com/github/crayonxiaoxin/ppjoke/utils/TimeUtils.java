package com.github.crayonxiaoxin.ppjoke.utils;

import java.util.Calendar;

public class TimeUtils {
    public static String calculate(long time) {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        long diff = (timeInMillis - time) / 1000; // 相差的秒数
        if (diff < 60) {
            return diff + "秒前";
        } else if (diff < 3600) {
            return (diff / 60) + "分钟前";
        } else if (diff < 24 * 3600) {
            return (diff / 3600) + "小时前";
        } else {
            return (diff / (3600 * 24)) + "天前";
        }
    }
}
