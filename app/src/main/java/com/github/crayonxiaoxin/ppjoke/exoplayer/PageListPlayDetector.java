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
                if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                    playingTarget.inActive();
                }
            }
        });
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            autoPlay();
        }
    };

    private void autoPlay() {
        if (mTargets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }
        // 如果上一个 target 还满足条件（正在播放并且在屏幕内），则继续播放
        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            Log.e("TAG", "autoPlay: old target" );
            return;
        }
        Log.e("TAG", "autoPlay: new target" );
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
