package com.github.crayonxiaoxin.ppjoke.ui.detail;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityFeedDetailTypeVideoBinding;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.view.FullScreenPlayerView;
import com.google.android.exoplayer2.ui.PlayerControlView;

public class VideoViewHandler extends ViewHandler {
    private CoordinatorLayout coordinator;
    private ActivityFeedDetailTypeVideoBinding mVideoBinding;
    private FullScreenPlayerView playerView;
    private String category;
    private boolean backPressed = false;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);
        mVideoBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_video);
        mRecyclerView = mVideoBinding.recyclerView;
        mInateractionBinding = mVideoBinding.bottomInteraction;
        playerView = mVideoBinding.playerView;
        coordinator = mVideoBinding.coordinator;

        View authorInfoRoot = mVideoBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) authorInfoRoot.getLayoutParams();
        params.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        mVideoBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
                Toast.makeText(mActivity, "close", Toast.LENGTH_SHORT).show();
            }
        });

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) layoutParams.getBehavior();
        behavior.setOnViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom = playerView.getBottom();
                boolean moveUp = height < bottom;
                int inputHeight = mInateractionBinding.getRoot().getMeasuredHeight();
                boolean fullscreen = !moveUp ? height >= coordinator.getBottom() - inputHeight - 2 : height >= coordinator.getBottom() - 2;
//                Log.e("TAG", "onDragZoom: fullscreen = " + fullscreen + " , up = " + moveUp);
//                Log.e("TAG", "onDragZoom: height = " + height);
//                Log.e("TAG", "onDragZoom: coordinator bottom = " + coordinator.getBottom());
//                Log.e("TAG", "onDragZoom: inputHeight = " + inputHeight);
                setViewAppearance(fullscreen);
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);
        mVideoBinding.setOwner(mActivity);

        category = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mVideoBinding.playerView.bindData(category, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        mVideoBinding.playerView.post(new Runnable() {
            @Override
            public void run() {
                // 是否正在进行视频的全屏展示
//                boolean fullscreen = mVideoBinding.playerView.getBottom() >= mVideoBinding.coordinator.getBottom() - 2;
                boolean fullscreen = mVideoBinding.playerView.getBottom() >= mVideoBinding.coordinator.getBottom() - mVideoBinding.bottomInteraction.getRoot().getMeasuredHeight() - 2;
                setViewAppearance(fullscreen);
                Log.e("TAG", "onDragZoom: fullscreen = " + fullscreen);
                Log.e("TAG", "onDragZoom: height = " + mVideoBinding.playerView.getBottom());
                Log.e("TAG", "onDragZoom: coordinator bottom = " + coordinator.getBottom());
                Log.e("TAG", "bindInitData: " + PixUtils.getScreenHeight()+" , " + mVideoBinding.getRoot().getBottom());
            }
        });

        LayoutFeedDetailTypeVideoHeaderBinding headerBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        headerBinding.setFeed(feed);
        listAdapter.addHeaderView(headerBinding.getRoot());
    }

    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mInateractionBinding.setFullscreen(fullscreen);
        // 如果是全屏播放，显示顶部的用户信息
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);
        // 如果是全屏播放，改变 controller 的位置
        int inputHeight = mInateractionBinding.getRoot().getMeasuredHeight();
        PlayerControlView playerControllerView = mVideoBinding.playerView.getPlayerControllerView();
        int ctrlHeight = playerControllerView.getMeasuredHeight();
        int ctrlBottom = playerControllerView.getBottom();
        playerControllerView.setY(fullscreen ? ctrlBottom - ctrlHeight - inputHeight : ctrlBottom - ctrlHeight);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        // 恢复 controller 的初始位置
        playerView.getPlayerControllerView().setTranslationY(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            playerView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        // 初次进入详情页时，需要等到 attach 之后才能 onActive，否则因 playerView 为 null 导致不会自动播放
        playerView.post(new Runnable() {
            @Override
            public void run() {
                playerView.onActive();
            }
        });
    }
}
