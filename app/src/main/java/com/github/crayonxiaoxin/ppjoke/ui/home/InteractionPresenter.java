package com.github.crayonxiaoxin.ppjoke.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.github.crayonxiaoxin.libcommon.AppGlobals;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.ppjoke.model.Comment;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.model.User;
import com.github.crayonxiaoxin.ppjoke.ui.ShareDialog;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.util.Date;


public class InteractionPresenter {

    public static final String URL_TOGGLE_FEED_LIKED = "/ugc/toggleFeedLike";
    public static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";
    public static final String URL_SHARE = "/ugc/increaseShareCount";
    public static final String URL_TOGGLE_COMMENT_LIKED = "/ugc/toggleCommentLike";

    public static void toggleFeedLiked(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedLikedInternal(feed);
                    }
                    loginLiveData.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedLikedInternal(feed);
    }

    private static void toggleFeedLikedInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKED)
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            try {
                                Log.e("TAG", "onSuccess: " + response);
                                boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                                feed.getUgc().setHasLike(hasLiked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedDissInternal(feed);
                    }
                    loginLiveData.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedDissInternal(feed);
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            try {
                                boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                                feed.getUgc().setHasdissd(hasLiked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    public static void openShareDialog(LifecycleOwner owner, Feed feed) {
        String url = "http://h5.aliyun.ppkoke.com/item/%s?timestamp=%s&user_id=%s";
        String format = String.format(url, feed.itemId, new Date().getTime(), UserManager.get().getUserId());
        Log.e("TAG", "openShareDialog: context null? " +( owner == null));
        ShareDialog shareDialog = new ShareDialog((Context) owner);
        shareDialog.setShareContent(format);
        shareDialog.setShareIemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApiService.get(URL_SHARE)
                        .addParam("itemId", feed.itemId)
                        .execute(new JsonCallback<JSONObject>() {
                            @Override
                            public void onSuccess(ApiResponse<JSONObject> response) {
                                if (response.body != null) {
                                    try {
                                        int count = response.body.getIntValue("count");
                                        feed.getUgc().setSharedCount(count);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });
        shareDialog.show();
    }

    public static void toggleCommentLiked(LifecycleOwner owner, Comment comment) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleCommentLikedInternal(comment);
                    }
                    loginLiveData.removeObserver(this);
                }
            });
            return;
        }
        toggleCommentLikedInternal(comment);
    }

    private static void toggleCommentLikedInternal(Comment comment) {
        ApiService.get(URL_TOGGLE_COMMENT_LIKED)
                .addParam("commentId", comment.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            try {
                                boolean hasLiked = response.body.getBooleanValue("hasLiked");
                                comment.getUgc().setHasLike(hasLiked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}
