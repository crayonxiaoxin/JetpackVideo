package com.github.crayonxiaoxin.libcommon.view;

import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.libcommon.R;

public class ViewHelper {
    public static final int RADIUS_ALL = 0;
    public static final int RADIUS_LEFT = 1;
    public static final int RADIUS_TOP = 2;
    public static final int RADIUS_RIGHT = 3;
    public static final int RADIUS_BOTTOM = 4;

    public static void setViewOutline(View view, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = view.getContext().obtainStyledAttributes(attrs, R.styleable.viewOutlineStrategy, defStyleAttr, defStyleRes);
        int radius = typedArray.getDimensionPixelOffset(R.styleable.viewOutlineStrategy_clipRadius, 0);
        int radiusSide = typedArray.getInt(R.styleable.viewOutlineStrategy_clipSide, RADIUS_ALL);
        typedArray.recycle();

        setViewOutline(view, radius, radiusSide);
    }

    public static void setViewOutline(View view, int radius, int radiusSide) {
        if (radius <= 0) return; // 不切圆角
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                if (width <= 0 || height <= 0) return;
                if (radiusSide != RADIUS_ALL) {
                    int left = 0, right = width, top = 0, bottom = height;
                    if (radiusSide == RADIUS_LEFT) {
                        right += radius; // 右边超出 view 本身后，右边就不是圆角了
                    } else if (radiusSide == RADIUS_TOP) {
                        bottom += radius;
                    } else if (radiusSide == RADIUS_RIGHT) {
                        left -= radius;
                    } else if (radiusSide == RADIUS_BOTTOM) {
                        top -= radius;
                    }
                    outline.setRoundRect(left, top, right, bottom, radius);
                } else {
                    outline.setRoundRect(0, 0, width, height, radius);
                }
            }
        });
        view.setClipToOutline(true);
        view.invalidate();
    }
}
