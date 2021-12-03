package com.github.crayonxiaoxin.ppjoke.exoplayer;

import android.graphics.Point;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PageListPlayDetector {

    private List<IPlayTarget> mTargets = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Point rvLocation = null;
    private IPlayTarget playingTarget;

    public void addTarget(IPlayTarget target) {
        mTargets.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        mTargets.remove(target);
    }

    public PageListPlayDetector(LifecycleOwner lifecycleOwner, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    playingTarget = null;
                    mTargets.clear();
                    recyclerView.removeCallbacks(delayAutoPlay);
                    if (recyclerView.getAdapter() != null) {
                        recyclerView.getAdapter().unregisterAdapterDataObserver(mDataObserver);
                    }
                    lifecycleOwner.getLifecycle().removeObserver(this);
                }
            }
        });
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    autoPlay();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dx == 0 && dy == 0) {
                    //时序问题。当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                    //所以此时 recyclerView.getChildCount()还是等于0的。
                    //等 childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                    postAutoPlay();
                } else {
                    if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                        playingTarget.inActive();
                    }
                }
            }
        });
    }

    private void postAutoPlay() {
        // View.post 保证 runnable 在 view 的 attachedToWindow 和 detachedToWindow 期间调用
        mRecyclerView.post(delayAutoPlay);
    }

    Runnable delayAutoPlay = new Runnable() {
        @Override
        public void run() {
            autoPlay();
        }
    };

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            postAutoPlay();
        }
    };

    private void autoPlay() {
        if (mTargets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }
        // 如果上一个 target 还满足条件（正在播放并且在屏幕内），则继续播放
        // 其实如果手动点击了播放按钮，那么 playingTarget 应该设置为对应的 target
        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            Log.e("TAG", "autoPlay: old target");
            return;
        }
        Log.e("TAG", "autoPlay: new target");
        // 否则，寻找新的符合要求的 target
        IPlayTarget activeTarget = null;
        for (IPlayTarget target : mTargets) {
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }
        if (activeTarget != null) {
            // 如果上一个 target 还在播放，则停止播放
            if (playingTarget != null && playingTarget.isPlaying()) {
                playingTarget.inActive();
            }
            // 新的 target 播放
            playingTarget = activeTarget;
            playingTarget.onActive();
        }
    }

    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }

        int[] location = new int[2];
        owner.getLocationOnScreen(location);

        int center = location[1] + owner.getHeight() / 2; // 容器的中心点位置

        return center >= rvLocation.x && center <= rvLocation.y; // 容器中心在 recyclerview 当中
    }

    private Point ensureRecyclerViewLocation() {
        if (rvLocation == null) { // 仅计算一次 recyclerview 的位置
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);
            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();
            rvLocation = new Point(top, bottom);
        }
        return rvLocation;
    }

    public void onPause() {
        if (playingTarget != null) {
            playingTarget.inActive();
        }
    }

    public void onResume() {
        if (playingTarget != null) {
            playingTarget.onActive();
        }
    }
}
