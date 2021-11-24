package com.github.crayonxiaoxin.ppjoke.ui.detail;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.libcommon.view.EmptyView;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedDetailBottomInateractionBinding;
import com.github.crayonxiaoxin.ppjoke.model.Comment;
import com.github.crayonxiaoxin.ppjoke.model.Feed;

public abstract class ViewHandler {
    private final FeedDetailViewModel viewModel;
    protected FragmentActivity mActivity;
    protected RecyclerView mRecyclerView;
    protected Feed mFeed;
    protected LayoutFeedDetailBottomInateractionBinding mInateractionBinding;
    protected FeedCommentAdapter listAdapter;

    public ViewHandler(FragmentActivity activity) {
        mActivity = activity;
        viewModel = new ViewModelProvider(activity).get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
//        mInateractionBinding.setOwner(mActivity);
//        mInateractionBinding.setFeed(feed);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        listAdapter = new FeedCommentAdapter(mActivity);
        mRecyclerView.setAdapter(listAdapter);
        viewModel.setItemId(mFeed.itemId);
        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                listAdapter.submitList(comments);
                handleEmpty(comments.size() > 0);
            }
        });
    }

    private EmptyView mEmptyView = null;

    private void handleEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                listAdapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                mEmptyView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
                listAdapter.addHeaderView(mEmptyView);
            }
        }
    }
}
