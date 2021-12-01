package com.github.crayonxiaoxin.ppjoke.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.ppjoke.R;

public class RecordView extends View implements View.OnClickListener, View.OnLongClickListener {
    private static final int PROGRESS_INTERVAL = 100; // 每个多少毫秒更新一次值
    private Paint fillPaint;
    private Paint progressPaint;
    private int progressColor;
    private int fillColor;
    private int progressWidth;
    private int radius;
    private int maxDuration;
    private int progressMaxValue;
    private boolean isRecording;
    private int progressValue = 0;
    private long startRecordTime;
    private onRecordListener mListener;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView);
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, 0);
        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, PixUtils.dp2px(3));
        maxDuration = typedArray.getInteger(R.styleable.RecordView_duration, 10);
        typedArray.recycle();
        setMaxDuration(maxDuration);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                progressValue++;
                postInvalidate();
                if (progressValue <= progressMaxValue) {
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                } else {
                    finishRecord();
                }
            }
        };
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isRecording = true;
                    startRecordTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(0);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    long now = System.currentTimeMillis();
                    // 超过了长按的时间，停止录制
                    if (now - startRecordTime > ViewConfiguration.getLongPressTimeout()) {
                        finishRecord();
                    }
                    handler.removeCallbacksAndMessages(null);
                    isRecording = false;
                    startRecordTime = 0;
                    progressValue = 0;
                    postInvalidate();
                }
                return false;
            }
        });

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    private void finishRecord() {
        if (mListener != null) {
            mListener.onFinish();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (isRecording) {
            canvas.drawCircle(width / 2f, height / 2f, width / 2f, fillPaint);
            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;
            float sweepAngle = (progressValue / progressMaxValue) * 360;
            canvas.drawArc(left, top, right, bottom, -90f, sweepAngle, false, progressPaint);
        } else {
            canvas.drawCircle(width / 2f, height / 2f, radius, fillPaint);
        }
    }

    private void setMaxDuration(int maxDuration) {
        this.progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    public void setonRecordListener(onRecordListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onClick();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (mListener != null) {
            mListener.onLongClick();
        }
        return true;
    }

    public interface onRecordListener {
        void onClick();

        void onLongClick();

        void onFinish();
    }
}
