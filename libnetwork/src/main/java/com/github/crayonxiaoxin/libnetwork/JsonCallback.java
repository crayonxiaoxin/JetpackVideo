package com.github.crayonxiaoxin.libnetwork;

public abstract class JsonCallback<T> {
    public void onSuccess(ApiResponse<T> response) {

    }

    public void onError(ApiResponse<T> response) {

    }

    public void cacheSuccess(ApiResponse<T> response) {

    }
}
