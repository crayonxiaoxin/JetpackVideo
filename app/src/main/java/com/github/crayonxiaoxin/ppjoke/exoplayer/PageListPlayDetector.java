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
        if (!mTargets.contains(target)) {
            mTargets.add(target);
        }
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
                    recyclerView.removeOnScrollListener(scrollListener);
                    lifecycleOwner.getLifecycle().removeObserver(this);
                }
            }
        });
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        }
        recyclerView.addOnScrollListener(scrollListener);
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (dx == 0 && dy == 0) {
                //???????????????????????????AdapterDataObserver#onItemRangeInserted  ???????????????????????????RecyclerView??????
                //???????????? recyclerView.getChildCount()????????????0??????
                //??? childView ????????????RecyclerView?????????????????????onScrolled????????????
                postAutoPlay();
            } else {
                if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                    playingTarget.inActive();
                }
            }
        }
    };

    private void postAutoPlay() {
        // View.post ?????? runnable ??? view ??? attachedToWindow ??? detachedToWindow ????????????
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
        // ??????????????? target ?????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????? playingTarget ???????????????????????? target
        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            Log.e("TAG", "autoPlay: old target");
            return;
        }
        Log.e("TAG", "autoPlay: new target");
        // ???????????????????????????????????? target
        IPlayTarget activeTarget = null;
        for (IPlayTarget target : mTargets) {
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }
        if (activeTarget != null) {
            // ??????????????? target ??????????????????????????????
            if (playingTarget != null && playingTarget.isPlaying()) {
                playingTarget.inActive();
            }
            // ?????? target ??????
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

        int center = location[1] + owner.getHeight() / 2; // ????????????????????????

        return center >= rvLocation.x && center <= rvLocation.y; // ??????????????? recyclerview ??????
    }

    private Point ensureRecyclerViewLocation() {
        if (rvLocation == null) { // ??????????????? recyclerview ?????????
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
