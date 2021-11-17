package com.github.crayonxiaoxin.libcommon;

import android.util.DisplayMetrics;

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
