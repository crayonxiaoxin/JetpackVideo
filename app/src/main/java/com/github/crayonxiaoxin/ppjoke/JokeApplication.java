package com.github.crayonxiaoxin.ppjoke;

import android.app.Application;

import com.github.crayonxiaoxin.libnetwork.ApiService;

public class JokeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://123.56.232.18:8080/serverdemo", null);
    }
}
