package com.github.crayonxiaoxin.ppjoke.ui.find;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.libcommon.view.EmptyView;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityTagFeedListBinding;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutTagFeedListHeaderBinding;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayDetector;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayManager;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.model.TagList;
import com.github.crayonxiaoxin.ppjoke.ui.home.FeedAdapter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

public class TagFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnRefreshListener, OnLoadMoreListener {
    public static final String KEY_TAG_LIST = "tag_list";
    public static final String KEY_FEED_TYPE = "tag_feed_type";
    private ActivityTagFeedListBinding mBinding;
    private RecyclerView recyclerView;
    private EmptyView emptyView;
    private SmartRefreshLayout refreshLayout;
    private TagList tagList;
    private PageListPlayDetector playDetector;
    private boolean shouldPause = true;
    private LayoutTagFeedListHeaderBinding headerBinding;
    private FeedAdapter adapter;
    private int totalScrollY = 0;
    private TagFeedListViewModel tagFeedListViewModel;

    public static void startActivity(Context context, TagList tagList) {
        Intent intent = new Intent(context, TagFeedListActivity.class);
        intent.putExtra(KEY_TAG_LIST, tagList);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tag_feed_list);
        recyclerView = mBinding.refreshLayoutView.recyclerView;
        emptyView = mBinding.refreshLayoutView.emptyView;
        refreshLayout = mBinding.refreshLayoutView.refreshLayout;

        mBinding.actionBack.setOnClickListener(this);

        tagList = (TagList) getIntent().getSerializableExtra(KEY_TAG_LIST);
        mBinding.setOwner(this);
        mBinding.setTagList(tagList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = getAdapter();
        recyclerView.setAdapter(adapter);
        playDetector = new PageListPlayDetector(this, recyclerView);

        addHeaderView();
        tagFeedListViewModel = new ViewModelProvider(this).get(TagFeedListViewModel.class);
        tagFeedListViewModel.setFeedType(tagList.title);
        tagFeedListViewModel.getPageData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            adapter.submitList(feeds);
        }
        finishRefresh(feeds.size() > 0);
    }

    private void finishRefresh(boolean hasData) {
        PagedList<Feed> currentList = adapter.getCurrentList();
        hasData = currentList != null && currentList.size() > 0 || hasData;
        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
        RefreshState state = refreshLayout.getState();
        if (state.isOpening && state.isHeader) {
            refreshLayout.finishRefresh();
        } else if (state.isOpening && state.isFooter) {
            refreshLayout.finishLoadMore();
        }
    }

    private void addHeaderView() {
        headerBinding = LayoutTagFeedListHeaderBinding.inflate(LayoutInflater.from(this), recyclerView, false);
        headerBinding.setTagList(tagList);
        adapter.addHeaderView(headerBinding.getRoot());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScrollY += dy;
                if (totalScrollY > PixUtils.dp2px(48)) {
                    mBinding.tagLogo.setVisibility(View.VISIBLE);
                    mBinding.tagTitle.setVisibility(View.VISIBLE);
                    mBinding.topBarFollow.setVisibility(View.VISIBLE);
                    mBinding.actionBack.setImageResource(R.drawable.icon_back_black);
                } else {
                    mBinding.tagLogo.setVisibility(View.GONE);
                    mBinding.tagTitle.setVisibility(View.GONE);
                    mBinding.topBarFollow.setVisibility(View.GONE);
                    mBinding.actionBack.setImageResource(R.drawable.icon_back_white);
                }
            }
        });
    }

    public FeedAdapter getAdapter() {
        return new FeedAdapter(this, KEY_FEED_TYPE) {
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
            public void onStartFeedDetailActivity(Feed feed) { // onPause时，是否需要暂停视频的播放（无缝续播）
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                // 这个方法是每提交一次 pagedList 都会触发
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        recyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_back) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shouldPause && playDetector != null) {
            playDetector.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playDetector != null) {
            playDetector.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        PageListPlayManager.release(KEY_FEED_TYPE);
        super.onDestroy();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        tagFeedListViewModel.getDataSource().invalidate();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        // 交给 paging 框架
    }
}
