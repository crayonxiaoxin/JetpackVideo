package com.github.crayonxiaoxin.libcommon.global;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppGlobals {
    private static Application sApplication;

    public static Application getApplication() {
        if (sApplication == null) {
            try {
                Method method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
                sApplication = (Application) method.invoke(null, new Object[]{});
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }
}
