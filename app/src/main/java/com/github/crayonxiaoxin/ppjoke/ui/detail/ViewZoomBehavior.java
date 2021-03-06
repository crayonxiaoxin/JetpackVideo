package com.github.crayonxiaoxin.ppjoke.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.recyclerview.widget.RecyclerView;

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
            refChild = (FullScreenPlayerView) child;
            childOriginalHeight = child.getMeasuredHeight();
            canFullscreen = childOriginalHeight > parent.getMeasuredWidth();
            Log.e("TAG", "onLayoutChild: childOriginalHeight" + childOriginalHeight + " " + parent.getMeasuredWidth());
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    private FlingRunnable runnable;
    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        // ?????? ViewDragHelper ???????????????????????? ?????????????????????view???????????????
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (!canFullscreen) return false;
            if (runnable != null) {
                refChild.removeCallbacks(runnable);
            }
            int refChildBottom = refChild.getBottom();
            if (child == refChild) {
                Log.e("TAG", "tryCaptureView: 1");
                return refChildBottom >= minHeight && refChildBottom <= childOriginalHeight;
            }
            if (child == scrollingView) {
                Log.e("TAG", "tryCaptureView: 2");
                return refChildBottom != minHeight && refChildBottom != childOriginalHeight;
            }
            Log.e("TAG", "tryCaptureView: 3");
            return false;
        }

        // ?????? ViewDragHelper ?????????????????????????????????????????????
        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return viewDragHelper.getTouchSlop();
        }

        // ?????? ViewDragHelper ?????????????????????view ???????????????????????????????????????
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (refChild == null || dy == 0) {
                return 0;
            }
            // dy>0 ??????????????????????????????????????????
            // dy<0 ??????????????????????????????????????????

            // ???????????????????????????refChild ????????????????????? minHeight
            if ((dy < 0 && refChild.getBottom() <= minHeight)
                    // ???????????????????????????refChild ????????????????????? childOriginalHeight
                    || (dy > 0 && refChild.getBottom() >= childOriginalHeight)
                    // ????????????????????????????????? scrollingView ??????????????????????????????????????????????????????????????????
                    || (dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1)))
            ) {
                return 0;
            }

            int maxConsumed = 0;
            if (dy > 0) {
                // ?????????????????????dy???
                if (refChild.getBottom() + dy > childOriginalHeight) {
                    maxConsumed = childOriginalHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            } else {
                // dy ?????????
                if (refChild.getBottom() + dy < minHeight) {
                    maxConsumed = minHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }

            // playerView ????????????
            ViewGroup.LayoutParams layoutParams = refChild.getLayoutParams();
            layoutParams.height = layoutParams.height + maxConsumed;
            refChild.setLayoutParams(layoutParams);
            if (mViewZoomCallback != null) {
                mViewZoomCallback.onDragZoom(layoutParams.height);
            }
            return maxConsumed;
        }

        // ???????????? ???????????????????????? ??????????????????????????????
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (refChild.getBottom() > minHeight && refChild.getBottom() < childOriginalHeight && yvel != 0) {
                // ????????????
                runnable = new FlingRunnable(refChild);
                runnable.fling((int) xvel, (int) yvel);
            }
        }

    };

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullscreen || viewDragHelper == null) {
            Log.e("TAG", "onTouchEvent: 1");
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
        // ???????????? ?????????????????????????????? ???????????????????????????
//        viewDragHelper.shouldInterceptTouchEvent(ev)
//        return true;
        // ???????????? viewDragHelper tryCaptureView ?????????
        return viewDragHelper.shouldInterceptTouchEvent(ev);
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
