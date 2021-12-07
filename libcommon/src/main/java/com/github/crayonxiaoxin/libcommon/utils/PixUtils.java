package com.github.crayonxiaoxin.libcommon.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.github.crayonxiaoxin.libcommon.global.AppGlobals;

public class PixUtils {
    private static boolean mHasCheckAllScreen;
    private static boolean mIsAllScreenDevice;

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
        if (isAllScreenDevice()) { // 全面屏的高度
            WindowManager windowManager = (WindowManager) AppGlobals.getApplication().getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                Point point = new Point();
                display.getRealSize(point);
                return point.y;
            }
        }
        // 非全面屏的高度
        return displayMetrics().heightPixels;
    }


    /**
     * 判断是否是全面屏
     *
     * @Author: Lau
     * @Date: 2021/12/7 10:56 上午
     */
    public static boolean isAllScreenDevice() {
        if (mHasCheckAllScreen) {
            return mIsAllScreenDevice;
        }
        mHasCheckAllScreen = true;
        mIsAllScreenDevice = false;
        // 低于 API 21的，都不会是全面屏。。。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        WindowManager windowManager = (WindowManager) AppGlobals.getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            float width, height;
            if (point.x < point.y) {
                width = point.x;
                height = point.y;
            } else {
                width = point.y;
                height = point.x;
            }
            if (height / width >= 1.97f) {
                mIsAllScreenDevice = true;
            }
        }
        return mIsAllScreenDevice;
    }

}
