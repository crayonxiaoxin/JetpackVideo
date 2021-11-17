package com.github.crayonxiaoxin.ppjoke.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.libcommon.PixUtils;
import com.github.crayonxiaoxin.ppjoke.R;

public class ListPlayerView extends FrameLayout {
    private PPImageView blurView;
    private PPImageView cover;
    private ImageView playBtn;
    private ProgressBar bufferView;
    private String mCategory;
    private String mVideoUrl;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);
        blurView = findViewById(R.id.blur_background);
        cover = findViewById(R.id.cover);
        playBtn = findViewById(R.id.play_btn);
        bufferView = findViewById(R.id.buffer_view);
    }

    public void bindData(String category, int widthPx, int HeightPx, String coverUrl, String videoUrl) {
        mCategory = category;
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

}
