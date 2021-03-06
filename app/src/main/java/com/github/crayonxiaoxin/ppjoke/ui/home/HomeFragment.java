package com.github.crayonxiaoxin.ppjoke.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayDetector;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.AbsListFragment;
import com.github.crayonxiaoxin.ppjoke.ui.MutablePageKeyedDataSource;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private PageListPlayDetector playDetector;
    private String feedType;
    private boolean shouldPause = true;

    public static HomeFragment newInstance(String feedType) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("feedType", feedType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        mViewModel.cacheLiveData.observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                adapter.submitList(feeds);
            }
        });
        playDetector = new PageListPlayDetector(this, mRecyclerView);
        mViewModel.setFeedType(feedType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType) {
            @Override
            public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                super.onViewDetachedFromWindow(holder);
                if (holder.isVideoItem()) {
                    playDetector.removeTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) { // onPause?????????????????????????????????????????????????????????
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                // ?????????????????????????????? pagedList ????????????
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        // ????????????????????????
        Feed feed = adapter.getCurrentList().get(adapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<? extends Feed> list) {
                PagedList.Config config = adapter.getCurrentList().getConfig();
                if (list != null && list.size() > 0) {
                    MutablePageKeyedDataSource<Integer, Feed> dataSource = new MutablePageKeyedDataSource<>();
                    dataSource.data.addAll(list);
                    dataSource.buildNewPagedList(config);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onPause() {
        // ?????????????????????????????????????????????
        if (shouldPause) {
            playDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        // ????????????????????????????????? fragment ????????????
        if (getParentFragment() != null) {
            if (getParentFragment().isVisible() && isVisible()) {
                playDetector.onResume();
            }
        } else {
            if (isVisible()) {
                playDetector.onResume();
            }
        }
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