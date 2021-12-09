package com.github.crayonxiaoxin.ppjoke.ui.my;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayDetector;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayManager;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.AbsListFragment;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

public class ProfileListFragment extends AbsListFragment<Feed, ProfileViewModel> {
    private String tabType;
    private PageListPlayDetector playDetector;
    private boolean shouldPause = true;

    public static ProfileListFragment newInstance(String tabType) {
        Bundle args = new Bundle();
        args.putString(ProfileActivity.KEY_TAB_TYPE, tabType);
        ProfileListFragment fragment = new ProfileListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        mViewModel.setProfileType(tabType);
        playDetector = new PageListPlayDetector(getViewLifecycleOwner(), mRecyclerView);
        binding.refreshLayout.setEnableRefresh(false);
    }

    @Override
    public PagedListAdapter getAdapter() {
        tabType = getArguments().getString(ProfileActivity.KEY_TAB_TYPE);
        return new ProfileListAdapter(getContext(), tabType) {
            @Override
            public void onViewAttachedToWindow2(@NonNull ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.removeTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                shouldPause = false;
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        if (playDetector != null) {
            // 评论页是没有视频的
            if (TextUtils.equals(tabType, ProfileActivity.TAB_TYPE_COMMENT)) {
                playDetector.onPause();
            } else {
                playDetector.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPause && playDetector != null) {
            playDetector.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        Log.e("TAG", "onDestroyView: " + tabType);
        PageListPlayManager.release(tabType);
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            playDetector.onPause();
        } else {
            playDetector.onResume();
        }
    }
}
