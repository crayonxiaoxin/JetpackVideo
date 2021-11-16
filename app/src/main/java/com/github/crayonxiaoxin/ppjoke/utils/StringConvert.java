package com.github.crayonxiaoxin.ppjoke.utils;

public class StringConvert {
    public static String convertFeedUgc(int count) {
        if (count < 1_0000) {
            return String.valueOf(count);
        }
        return count / 10000 + "万";
    }
}
