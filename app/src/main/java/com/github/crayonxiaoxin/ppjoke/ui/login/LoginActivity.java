package com.github.crayonxiaoxin.ppjoke.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.crayonxiaoxin.libcommon.AppGlobals;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityLayoutLoginBinding;
import com.github.crayonxiaoxin.ppjoke.model.User;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private ActivityLayoutLoginBinding binding;
    private Tencent tencent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLayoutLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Tencent.setIsPermissionGranted(true);

        binding.actionClose.setOnClickListener(view -> close());
        binding.actionLogin.setOnClickListener(view -> loginByQQ());
    }

    public void close() {
        finish();
    }

    IUiListener qqLoginListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            JSONObject response = (JSONObject) o;
            try {
                String openid = response.getString("openid");
                String access_token = response.getString("access_token");
                String expires_in = response.getString("expires_in");
                tencent.setAccessToken(access_token, expires_in);
                tencent.setOpenId(openid);
                getUserInfo(tencent.getQQToken(), expires_in, openid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "登录失败，原因：" + uiError.errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onCancel() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "登录取消", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onWarning(int i) {

        }
    };

    public void loginByQQ() {
        if (tencent == null) {
            tencent = Tencent.createInstance("101980089", AppGlobals.getApplication());
        }
        if (!tencent.isSessionValid()) {
            tencent.login(this, "all", qqLoginListener);
        }
    }

    private void getUserInfo(QQToken qqToken, String expires_in, String openid) {
        UserInfo userInfo = new UserInfo(this, qqToken);
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                JSONObject response = (JSONObject) o;
                try {
                    String nickname = response.getString("nickname");
                    String figureurl_2 = response.getString("figureurl_2");
                    save(nickname, figureurl_2, openid, expires_in);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "登录失败，原因：" + uiError.errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancel() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "登录取消", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWarning(int i) {

            }
        });
    }

    private void save(String nickname, String figureurl_2, String openid, String expires_in) {
        ApiService.get("/user/insert")
                .addParam("avatar", figureurl_2)
                .addParam("expires_time", expires_in)
                .addParam("name", nickname)
                .addParam("qqOpenId", openid)
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        if (response != null) {
                            UserManager.get().save(response.body);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    close();
                                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void cacheSuccess(ApiResponse<User> response) {
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(resultCode, resultCode, data, qqLoginListener);
        }
    }
}
