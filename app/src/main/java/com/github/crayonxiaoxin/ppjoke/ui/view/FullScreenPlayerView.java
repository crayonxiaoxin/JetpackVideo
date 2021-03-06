package com.github.crayonxiaoxin.ppjoke.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlay;
import com.github.crayonxiaoxin.ppjoke.exoplayer.PageListPlayManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class FullScreenPlayerView extends ListPlayerView {
    private PlayerView exoPlayerView;

    public FullScreenPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        exoPlayerView = (PlayerView) LayoutInflater.from(context).inflate(R.layout.layout_exo_player_view, null, false);
    }

    @Override
    protected void setSize(int widthPx, int heightPx) {
        if (widthPx >= heightPx) {
            super.setSize(widthPx, heightPx);
        } else {
            int maxWidth = PixUtils.getScreenWidth();
            int maxHeight = PixUtils.getScreenHeight();
            Log.e("TAG", "setSize: maxWidth=" + maxWidth + ", maxHeight=" + maxHeight);

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = maxWidth;
            layoutParams.height = maxHeight;
            setLayoutParams(layoutParams);
            setBackgroundColor(Color.BLUE);

            FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
            coverParams.width = (int) (widthPx / (heightPx * 1.0f / maxHeight));
            coverParams.height = maxHeight;
            coverParams.gravity = Gravity.CENTER;
            cover.setLayoutParams(coverParams);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (mWidthPx < mHeightPx) {
            int layoutWidth = params.width;
            int layoutHeight = params.height;
            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            coverParams.width = (int) (mWidthPx / (mHeightPx * 1.0f / layoutHeight));
            coverParams.height = layoutHeight;
            cover.setLayoutParams(coverParams);

            if (exoPlayerView != null) {
                ViewGroup.LayoutParams layoutParams = exoPlayerView.getLayoutParams();
                if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
                    float scaleX = coverParams.width * 1.0f / layoutParams.width;
                    float scaleY = coverParams.height * 1.0f / layoutParams.height;
                    exoPlayerView.setScaleX(scaleX);
                    exoPlayerView.setScaleY(scaleY);
                }
            }
        }
        super.setLayoutParams(params);
    }

    @Override
    public void onActive() {
        Log.e("TAG", "onActive: ");
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        ExoPlayer exoPlayer = pageListPlay.exoPlayer;
        PlayerView playerView = exoPlayerView; // ?????? playerView
        PlayerControlView controllerView = pageListPlay.controllerView;
        if (playerView == null) {
            return;
        }

        pageListPlay.switchPlayerView(playerView, true);

        ViewParent parent = playerView.getParent();
        if (parent != this) { // ?????? playerView ??????????????? this
            if (parent != null) { // ?????? playerView ??? ViewParent ??????
                ((ViewGroup) parent).removeView(playerView);
//                // ??????????????????????????????????????????????????????
//                ((ListPlayerView) parent).inActive();
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
        } else { // ???????????????????????????
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            pageListPlay.playUrl = mVideoUrl; // ????????????????????????????????????????????????else????????????????????????????????????
        }
        controllerView.show();
        controllerView.addVisibilityListener(this); // ?????????????????? controller ??????/??????

        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); // ?????????????????????
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void inActive() {
        Log.e("TAG", "inActive: ");
        super.inActive();
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        // ??????????????????????????????????????????????????????
        pageListPlay.switchPlayerView(exoPlayerView, false);
    }
}
