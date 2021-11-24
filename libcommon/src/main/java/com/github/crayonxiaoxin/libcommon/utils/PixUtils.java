package com.github.crayonxiaoxin.libcommon.utils;

import android.util.DisplayMetrics;

import com.github.crayonxiaoxin.libcommon.global.AppGlobals;

public class PixUtils {
    public static DisplayMetrics displayMetrics() {
        return AppGlobals.getApplication().getResources().getDisplayMetrics();
    }

    public static int dp2px(int dp) {
        DisplayMetrics displayMetrics = displayMetrics();
        return (int) (displayMetrics.density * dp + 0.5f);
    }

    public static int getScreenWidth() {
        return displayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return displayMetrics().heightPixels;
    }
}
