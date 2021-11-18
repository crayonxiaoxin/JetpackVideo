package com.github.crayonxiaoxin.ppjoke.ui.login;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.crayonxiaoxin.libnetwork.cache.CacheManager;
import com.github.crayonxiaoxin.ppjoke.model.User;

public class UserManager {

    public static final String KEY_CACHE_USER = "cache_user";
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private static UserManager userManager = new UserManager();
    private User mUser;

    public static UserManager get() {
        return userManager;
    }

    public UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null && cache.expiresTime < System.currentTimeMillis()) {
            mUser = cache;
        }
    }

    public void save(User user) {
        mUser = user;
        CacheManager.save(KEY_CACHE_USER, user);
        if (userLiveData.hasObservers()) {
            userLiveData.postValue(user);
        }
    }

    public LiveData<User> login(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        return userLiveData;
    }

    public boolean isLogin() {
        return mUser != null;
    }

    public User getUser() {
        return isLogin() ? mUser : null;
    }

    public long getUserId() {
        return isLogin() ? mUser.userId : 0;
    }
}
