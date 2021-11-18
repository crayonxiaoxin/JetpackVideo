package com.github.crayonxiaoxin.ppjoke.ui.login;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.crayonxiaoxin.libcommon.AppGlobals;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityLayoutLoginBinding;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class LoginActivity extends AppCompatActivity {
    private ActivityLayoutLoginBinding binding;

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

    public void loginByQQ() {
        Tencent tencent = Tencent.createInstance("101980089", AppGlobals.getApplication());
        if (!tencent.isSessionValid()){
            tencent.login(this, "all", new IUiListener() {
                @Override
                public void onComplete(Object o) {

                }

                @Override
                public void onError(UiError uiError) {

                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onWarning(int i) {

                }
            });
        }
    }
}
