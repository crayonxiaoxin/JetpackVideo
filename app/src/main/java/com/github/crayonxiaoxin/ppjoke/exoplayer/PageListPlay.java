package com.github.crayonxiaoxin.ppjoke.exoplayer;

import android.app.Application;
import android.util.Log;
import android.view.LayoutInflater;

import com.github.crayonxiaoxin.libcommon.global.AppGlobals;
import com.github.crayonxiaoxin.ppjoke.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class PageListPlay {
    public PlayerControlView controllerView;
    public ExoPlayer exoPlayer;
    public PlayerView playerView;
    public String playUrl;

    public PageListPlay() {
        Application application = AppGlobals.getApplication();

        exoPlayer = new ExoPlayer.Builder(application)
                .setRenderersFactory(new DefaultRenderersFactory(application))
                .setTrackSelector(new DefaultTrackSelector())
                .setLoadControl(new DefaultLoadControl.Builder().setPrioritizeTimeOverSizeThresholds(false).build())
                .build();
        playerView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_view, null, false);
        controllerView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_controller_view, null, false);

        playerView.setPlayer(exoPlayer);
        controllerView.setPlayer(exoPlayer);
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }
        if (controllerView != null) {
            controllerView.setPlayer(null);
//            controllerView.removeVisibilityListener();
            controllerView = null;
        }
    }

    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        if (attach) {
            // 停止旧播放器的关联
            this.playerView.setPlayer(null);
            // 给传递进来的 playerView 配置播放器
            newPlayerView.setPlayer(this.exoPlayer);
        } else {
            // 恢复旧播放器的关联
            this.playerView.setPlayer(this.exoPlayer);
            // 停止新播放器的关联
            newPlayerView.setPlayer(null);
        }
    }
}
