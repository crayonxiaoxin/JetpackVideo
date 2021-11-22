package com.github.crayonxiaoxin.ppjoke.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;

import com.github.crayonxiaoxin.libcommon.AppGlobals;
import com.github.crayonxiaoxin.ppjoke.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class PageListPlay {
    public PlayerControlView controllerView;
    public ExoPlayer exoPlayer;
    public PlayerView playerView;
    public String playUrl;

    public PageListPlay() {
        Application application = AppGlobals.getApplication();

        exoPlayer = new ExoPlayer.Builder(application).build();
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
}
