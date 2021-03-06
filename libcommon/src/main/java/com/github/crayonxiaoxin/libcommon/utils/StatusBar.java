package com.github.crayonxiaoxin.libcommon.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, decorFitsSystemWindows);
        } else {
            // 修复 WindowCompat.setDecorFitsSystemWindows 切换导致界面跳动的问题
            // api<30 会添加 flag： SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION（问题的根源）
            final int decorFitsFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            final int sysUiVis = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(decorFitsSystemWindows
                    ? sysUiVis & ~decorFitsFlags
                    : sysUiVis | decorFitsFlags);
        }
        // 绘制 statusBar 透明色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        // 设置 LightStatusBars 文字颜色
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(decorView);
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(darkIcons); // 状态栏文字图标颜色
            if (fullscreen) {
                windowInsetsController.hide(WindowInsetsCompat.Type.statusBars()); // 隐藏状态栏
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 适配刘海屏无法全屏（状态栏填充黑色）的问题
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    window.setAttributes(layoutParams);
                }
            }
        } else {
            // 修复 api<30 无法切换 亮色或暗色 状态栏的问题
            int visibility = decorView.getSystemUiVisibility();
            if (darkIcons) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(visibility);
        }
    }
}
