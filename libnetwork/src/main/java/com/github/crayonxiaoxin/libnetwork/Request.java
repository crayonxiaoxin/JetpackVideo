package com.github.crayonxiaoxin.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.github.crayonxiaoxin.libnetwork.cache.CacheManager;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class Request<T, R extends Request> implements Cloneable {
    protected String mUrl;
    protected HashMap<String, String> headers = new HashMap<>();
    protected HashMap<String, Object> params = new HashMap<>();

    // 仅访问缓存
    public static final int CACHE_ONLY = 1;
    // 先访问缓存，再访问网络
    public static final int CACHE_FIRST = 2;
    // 仅访问网络，不缓存
    public static final int NET_ONLY = 3;
    // 先访问网络，再缓存
    public static final int NET_CACHE = 4;
    private String cacheKey;
    private Type mType;
    private Class mClazz;
    private int mCacheStrategy;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {
    }

    public Request(String url) {
        this.mUrl = url;
    }

    public R addHeader(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) value = "";
        params.put(key, value);
        return (R) this;
    }

    public R cacheKey(String key) {
        cacheKey = key;
        return (R) this;
    }

    // 为同步方式制定返回值的类型
    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseType(Class clazz) {
        mClazz = clazz;
        return (R) this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    // 异步
    @SuppressLint("RestrictedApi")
    public void execute(JsonCallback<T> callback) {
        if (mCacheStrategy != NET_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null && response != null && response.body != null) {
                        Log.e("TAG", "run: " + response.status);
                        callback.cacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    ApiResponse<T> response = new ApiResponse<>();
                    response.message = e.getMessage();
                    callback.onError(response);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response, callback);
                    if (apiResponse.success) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse);
                    }
                }
            });
        }
    }

    // 同步
    public ApiResponse<T> execute() {
        if (mCacheStrategy == CACHE_ONLY) {
            return readCache();
        }
        try {
            Response response = getCall().execute();
            return parseResponse(response, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.body = (T) cache;
        result.success = true;
        result.message = "缓存成功";
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Converter converter = ApiService.sConverter;
        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    // 从匿名内部类callback中获取泛型的类型，详见java类型擦除
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) converter.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) converter.convert(content, mType);
                } else if (mClazz != null) {
                    result.body = (T) converter.convert(content, mClazz);
                } else {
                    Log.e("Request", "parseResponse: 无法解析");
                }
            } else {
                message = content;
            }
        } catch (IOException e) {
            e.printStackTrace();
            message = e.getMessage();
            success = false;
        }
        result.success = success;
        result.status = status;
        result.message = message;
        if (mCacheStrategy != NET_ONLY && result.success && result.body != null && result.body instanceof Serializable) {
            saveCache(result.body);
            Log.e("TAG", "parseResponse: save");
        }
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return cacheKey;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        return ApiService.okHttpClient.newCall(request);
    }

    // GET 和 POST 的参数拼接方式不同
    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }
}
