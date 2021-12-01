package com.github.crayonxiaoxin.ppjoke.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityPreviewBinding;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;


public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityPreviewBinding mBinding;
    public static String KEY_PREVIEW_URI = "preview_uri";
    public static String KEY_PREVIEW_VIDEO = "preview_video";
    public static String KEY_PREVIEW_BTN_TEXT = "preview_btn_text";
    private ExoPlayer player;

    public static Intent intentActivityForResult(Activity activity, Uri previewUri, boolean isVideo, String btnText) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URI, previewUri);
        intent.putExtra(KEY_PREVIEW_VIDEO, isVideo);
        intent.putExtra(KEY_PREVIEW_BTN_TEXT, btnText);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_preview);
        Intent intent = getIntent();
        Uri previewUri = intent.getParcelableExtra(KEY_PREVIEW_URI);
        String btnText = intent.getStringExtra(KEY_PREVIEW_BTN_TEXT);
        boolean isVideo = intent.getBooleanExtra(KEY_PREVIEW_VIDEO, false);
        if (TextUtils.isEmpty(btnText)) {
            mBinding.actionOk.setVisibility(View.GONE);
        } else {
            mBinding.actionOk.setVisibility(View.VISIBLE);
            mBinding.actionOk.setText(btnText);
            mBinding.actionOk.setOnClickListener(this);
        }
        mBinding.actionClose.setOnClickListener(this);
        if (isVideo) {
            previewVideo(previewUri);
        } else {
            previewImage(previewUri);
        }
    }

    private void previewImage(Uri previewUri) {
        mBinding.photoView.setVisibility(View.VISIBLE);
        Glide.with(this).load(previewUri).into(mBinding.photoView);
    }

    private void previewVideo(Uri previewUri) {
        mBinding.playerView.setVisibility(View.VISIBLE);
        player = new ExoPlayer.Builder(this)
                .setRenderersFactory(new DefaultRenderersFactory(this))
                .setTrackSelector(new DefaultTrackSelector())
                .setLoadControl(new DefaultLoadControl())
                .build();
        File file = new File(previewUri.getPath());
        Uri uri = null;
        if (file.exists()) {
            DataSpec dataSpec = new DataSpec.Builder().setUri(Uri.fromFile(file)).build();
            FileDataSource dataSource = new FileDataSource.Factory().createDataSource();
            try {
                dataSource.open(dataSpec);
                uri = dataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        } else {
            uri = previewUri;
        }
        if (uri == null) return;
        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(this));
        ProgressiveMediaSource mediaSource = factory.createMediaSource(MediaItem.fromUri(uri));
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        mBinding.playerView.setPlayer(player);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.action_close) {
            finish();
        } else if (id == R.id.action_ok) {
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }
}
