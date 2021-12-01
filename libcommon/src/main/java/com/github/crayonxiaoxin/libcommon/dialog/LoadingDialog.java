package com.github.crayonxiaoxin.libcommon.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.github.crayonxiaoxin.libcommon.R;

public class LoadingDialog extends AlertDialog {
    private TextView loadingText;

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setLoadingText(String loadingText) {
        if (this.loadingText != null) {
            this.loadingText.setText(loadingText);
        }
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.layout_loading_view);
        loadingText = findViewById(R.id.loading_text);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        attributes.dimAmount = 0.35f;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(attributes);
    }
}
