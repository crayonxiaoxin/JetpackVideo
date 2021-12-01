package com.github.crayonxiaoxin.ppjoke.ui.detail;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityFeedDetailTypeImageBinding;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.view.PPImageView;

public class ImageViewHandler extends ViewHandler {
    protected final ActivityFeedDetailTypeImageBinding mImageBinding;
    private LayoutFeedDetailTypeImageHeaderBinding mHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);
        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);
        mRecyclerView = mImageBinding.recyclerView;
        mInateractionBinding = mImageBinding.interactionLayout;
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);

        // 之前 setFeed 写在了 constructor，导致 底部互动区 空指针
        mImageBinding.setFeed(mFeed);
        mImageBinding.setOwner(mActivity);

        mHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        mHeaderBinding.setFeed(feed);
        PPImageView headerImage = mHeaderBinding.headerImage;
        headerImage.bindData(mFeed.width, mFeed.height, mFeed.width > mFeed.height ? 0 : 16, mFeed.cover);
        listAdapter.addHeaderView(mHeaderBinding.getRoot());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 图文区域 移动距离是否已经超过了 title 区域的高度
                boolean visible = mHeaderBinding.getRoot().getTop() <= -mImageBinding.titleLayout.getMeasuredHeight();
                mImageBinding.authorInfoLayout.setFeed(feed);
                mImageBinding.authorInfoLayout.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
                mImageBinding.title.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });
    }

}
