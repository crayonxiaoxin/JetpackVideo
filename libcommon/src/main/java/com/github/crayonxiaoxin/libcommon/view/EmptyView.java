package com.github.crayonxiaoxin.libcommon.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.libcommon.R;

public class EmptyView extends LinearLayout {
    private ImageView icon;
    private Button action;
    private TextView title;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_empty_view, this, true);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        action = findViewById(R.id.empty_action);
        icon = findViewById(R.id.empty_icon);
        title = findViewById(R.id.empty_text);
    }

    public void setEmptyIcon(@DrawableRes int resId) {
        icon.setImageResource(resId);
    }

    public void setTitle(String text) {
        if (TextUtils.isEmpty(text)) {
            title.setVisibility(GONE);
        } else {
            title.setVisibility(VISIBLE);
            title.setText(text);
        }
    }

    public void setButton(String text, View.OnClickListener listener) {
        if (TextUtils.isEmpty(text)) {
            action.setVisibility(GONE);
        } else {
            action.setVisibility(VISIBLE);
            action.setText(text);
            action.setOnClickListener(listener);
        }
    }
}
