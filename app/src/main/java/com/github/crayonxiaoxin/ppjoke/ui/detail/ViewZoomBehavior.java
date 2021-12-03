package com.github.crayonxiaoxin.ppjoke.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.ui.view.FullScreenPlayerView;

public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullScreenPlayerView> {
    private OverScroller overScroller;
    private int minHeight;
    private int scrollingId;
    private ViewDragHelper viewDragHelper;
    private View scrollingView;
    private FullScreenPlayerView refChild;
    private int childOriginalHeight;
    private boolean canFullscreen;
    private ViewZoomCallback mViewZoomCallback;

    public ViewZoomBehavior() {
    }

    public ViewZoomBehavior(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.view_zoom_behavior);
        scrollingId = typedArray.getResourceId(R.styleable.view_zoom_behavior_scrolling_id, 0);
        minHeight = typedArray.getDimensionPixelOffset(R.styleable.view_zoom_behavior_min_height, PixUtils.dp2px(200));
        typedArray.recycle();
        overScroller = new OverScroller(context);
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, int layoutDirection) {
        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback);
            scrollingView = parent.findViewById(scrollingId);
            refChild = child;
            childOriginalHeight = child.getMeasuredHeight();
            canFullscreen = childOriginalHeight > parent.getMeasuredWidth();
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        // 告诉 ViewDragHelper 什么时候可以拦截 手指触摸的这个view的手势分发
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (canFullscreen && refChild.getBottom() >= minHeight) {
                return true;
            }
            return false;
        }

        // 告诉 ViewDragHelper 在屏幕上滑动多少距离才算是拖拽
        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return viewDragHelper.getTouchSlop();
        }

        // 告诉 ViewDragHelper 手指拖拽的这个view 本次滑动最终能够滑动的距离
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (refChild == null || dy == 0) {
                return 0;
            }
            // dy>0 代表手指从屏幕上方往下方滑动
            // dy<0 代表手指从屏幕下方往上方滑动

            // 手指从下往上滑动。refChild 的底部不能小于 minHeight
            if ((dy < 0 && refChild.getBottom() < minHeight)
                    // 手指从上往下滑动。refChild 的底部不能超过 childOriginalHeight
                    || (dy > 0 && refChild.getBottom() > childOriginalHeight)
                    // 手指从上往下滑动。如果 scrollingView 还没滑动到顶部。此时滑动事件应该交由列表处理
                    || (dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1)))
            ) {
                return 0;
            }

            int maxConsumed = 0;
            if (dy > 0) {
                // 如果本次滑动的dy值
                if (refChild.getBottom() + dy > childOriginalHeight) {
                    maxConsumed = childOriginalHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            } else {
                // dy 是负值
                if (refChild.getBottom() + dy < minHeight) {
                    maxConsumed = minHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }

            // playerView 等比缩放
            ViewGroup.LayoutParams layoutParams = refChild.getLayoutParams();
            layoutParams.height = layoutParams.height + maxConsumed;
            refChild.setLayoutParams(layoutParams);
            if (mViewZoomCallback != null) {
                mViewZoomCallback.onDragZoom(layoutParams.height);
            }
            return maxConsumed;
        }

        // 指我们的 手指从屏幕上离开 的时候会调用这个方法
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (refChild.getBottom() > minHeight && refChild.getBottom() < childOriginalHeight && yvel != 0) {
                // 惯性滑动
                FlingRunnable flingRunnable = new FlingRunnable(refChild);
                flingRunnable.fling((int) xvel, (int) yvel);
            }
        }

    };

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullscreen || viewDragHelper == null) {
            return super.onTouchEvent(parent, child, ev);
        }
        viewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullscreen || viewDragHelper == null) {
            return super.onInterceptTouchEvent(parent, child, ev);
        }
        viewDragHelper.shouldInterceptTouchEvent(ev);
        return true;
    }

    public void setOnViewZoomCallback(ViewZoomCallback callback) {
        mViewZoomCallback = callback;
    }

    public interface ViewZoomCallback {
        void onDragZoom(int height);
    }

    private class FlingRunnable implements Runnable {
        private View mFlingView;

        public FlingRunnable(View view) {
            mFlingView = view;
        }

        public void fling(int xvel, int yvel) {
            overScroller.fling(0, mFlingView.getBottom(), xvel, xvel, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            run();
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams layoutParams = mFlingView.getLayoutParams();
            int height = layoutParams.height;
            if (overScroller.computeScrollOffset() && height >= minHeight && height <= childOriginalHeight) {
                int newHeight = Math.min(overScroller.getCurrY(), childOriginalHeight);
                if (newHeight != height) {
                    layoutParams.height = newHeight;
                    mFlingView.setLayoutParams(layoutParams);
                    if (mViewZoomCallback != null) {
                        mViewZoomCallback.onDragZoom(newHeight);
                    }
                }
                ViewCompat.postOnAnimation(mFlingView, this);
            } else {
                mFlingView.removeCallbacks(this);
            }
        }
    }
}
