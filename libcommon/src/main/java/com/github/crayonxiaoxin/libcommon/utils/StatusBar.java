package com.github.crayonxiaoxin.libcommon.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class StatusBar {
    public static void fitSystemBar(Activity activity) {
        fitSystemBar(activity, true);
    }

    public static void fitSystemBar(Activity activity, boolean decorFitsSystemWindows) {
        fitSystemBar(activity, decorFitsSystemWindows, true, false);
    }

    public static void fitSystemBar(Activity activity, boolean decorFitsSystemWindows, boolean fullscreen) {
        fitSystemBar(activity, decorFitsSystemWindows, true, fullscreen);
    }

    public static void fitSystemBar(Activity activity, boolean decorFitsSystemWindows, boolean darkIcons, boolean fullscreen) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        // false 状态栏覆盖在 fitSystemBar 之上，true 不会覆盖
        WindowCompat.setDecorFitsSystemWindows(window, decorFitsSystemWindows);
        // 设置 statusBar 透明
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        // 设置 LightStatusBars 文字颜色
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(decorView);
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(darkIcons);
            if (fullscreen) {
                windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 适配刘海屏无法全屏（状态栏填充黑色）的问题
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    window.setAttributes(layoutParams);
                }
            }
        }
    }
}
