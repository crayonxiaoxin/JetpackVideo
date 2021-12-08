package com.github.crayonxiaoxin.ppjoke.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class StringConvert {
    public static String convertFeedUgc(int count) {
        if (count < 1_0000) {
            return String.valueOf(count);
        }
        return count / 10000 + "万";
    }

    public static String convertTagFeedList(int count) {
        if (count < 1_0000) {
            return String.valueOf(count) + "人观看";
        }
        return count / 10000 + "万人观看";
    }

    public static String convertSpannable(int count, String desc) {
        String countStr = String.valueOf(count);
        SpannableString ss = new SpannableString(countStr + desc);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, countStr.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new AbsoluteSizeSpan(16), 0, countStr.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, countStr.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss.toString();
    }
}
