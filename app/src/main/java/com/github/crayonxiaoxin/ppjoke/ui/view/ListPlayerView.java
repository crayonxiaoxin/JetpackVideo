package com.github.crayonxiaoxin.ppjoke.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.exoplayer.IPlayTarget;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlay;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class ListPlayerView extends FrameLayout implements IPlayTarget, PlayerControlView.VisibilityListener, Player.Listener {
    protected PPImageView blurView;
    protected PPImageView cover;
    protected ImageView playBtn;
    protected ProgressBar bufferView;
    protected String mCategory;
    protected int mWidthPx;
    protected int mHeightPx;
    protected String mVideoUrl;
    protected boolean isPlaying = false;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);
        blurView = findViewById(R.id.blur_background);
        cover = findViewById(R.id.cover);
        playBtn = findViewById(R.id.play_btn);
        bufferView = findViewById(R.id.buffer_view);
        playBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying()) {
                    inActive();
                } else {
                    onActive();
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.controllerView.show();
        return true;
    }

    public void bindData(String category, int widthPx, int HeightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mWidthPx = widthPx;
        mHeightPx = HeightPx;
        mVideoUrl = videoUrl;
        PPImageView.setImageUrl(cover, coverUrl, false);
        if (widthPx < HeightPx) {
            blurView.setBlurImageUrl(coverUrl, 10);
            blurView.setVisibility(VISIBLE);
        } else {
            blurView.setVisibility(INVISIBLE);
        }
        setSize(widthPx, HeightPx);
    }

    protected void setSize(int widthPx, int heightPx) {
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = maxWidth;

        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        int coverWidth;
        int coverHeight;

        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx / (widthPx * 1.0f / maxWidth));
        } else {
            coverHeight = maxHeight;
            layoutWidth = coverWidth = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        }

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = layoutWidth;
        layoutParams.height = layoutHeight;
        setLayoutParams(layoutParams);

        ViewGroup.LayoutParams blurParams = blurView.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeight;
        blurView.setLayoutParams(blurParams);

        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        FrameLayout.LayoutParams playParams = (LayoutParams) playBtn.getLayoutParams();
        playParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playParams);

    }

    @Override
    public ViewGroup getOwner() { // playerView 的容器
        return this;
    }

    @Override
    public void onActive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        ExoPlayer exoPlayer = pageListPlay.exoPlayer;
        PlayerView playerView = pageListPlay.playerView;
        PlayerControlView controllerView = pageListPlay.controllerView;
        if (playerView == null) {
            return;
        }
        pageListPlay.switchPlayerView(playerView, true);

        ViewParent parent = playerView.getParent();
        if (parent != this) { // 如果 playerView 父容器不是 this
            if (parent != null) { // 如果 playerView 在 ViewParent 里面
                ((ViewGroup) parent).removeView(playerView);
                // 暂停正在播放的
                ((ListPlayerView) parent).inActive();
            }
            ViewGroup.LayoutParams layoutParams = cover.getLayoutParams();
            this.addView(playerView, 1, layoutParams);
        }
        ViewParent ctrlParent = controllerView.getParent();
        if (ctrlParent != this) {
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controllerView);
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM;
            this.addView(controllerView, layoutParams);
        }

        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else { // 不是同一个视频资源
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); // 重复播放这一个
            pageListPlay.playUrl = mVideoUrl; // 这里之前没有赋值，导致每次都进入else分支，每次播放都重头开始
        }
        controllerView.show();
        controllerView.addVisibilityListener(this); // 播放按钮跟随 controller 隐藏/显示

        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public void inActive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        ExoPlayer exoPlayer = pageListPlay.exoPlayer;
        PlayerControlView controllerView = pageListPlay.controllerView;
        if (exoPlayer == null || controllerView == null) return;
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.removeListener(this); // 不移除的话，当一个item player状态改变时，所有item的状态都会改变
        controllerView.removeVisibilityListener(this); // 不移除的话，当controller出现时，所有item的play都会出现
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.e("TAG", "onPlayerStateChanged: " + playWhenReady + " " + playbackState);
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        ExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            cover.setVisibility(GONE);
            Log.e("TAG", "onPlayerStateChanged: " + (cover.getVisibility() == GONE));
            bufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            bufferView.setVisibility(VISIBLE);
        }
        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public PlayerControlView getPlayerControllerView() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        return pageListPlay.controllerView;
    }
}
