package com.github.crayonxiaoxin.ppjoke.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget {
    ViewGroup getOwner(); // playerView 的父容器

    void onActive(); // 播放

    void inActive(); // 停止

    boolean isPlaying();
}
