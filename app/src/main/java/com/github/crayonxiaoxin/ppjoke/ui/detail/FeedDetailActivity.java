package com.github.crayonxiaoxin.ppjoke.ui.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.github.crayonxiaoxin.libcommon.utils.StatusBar;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {
    private static final String KEY_FEED = "key_feed";
    private static final String KEY_CATEGORY = "key_category";

    public static void startFeedDetailActivity(Context mContext, Feed item, String category) {
        Log.e("TAG", "startFeedDetailActivity: ");
        Intent intent = new Intent(mContext, FeedDetailActivity.class);
        intent.putExtra(KEY_FEED, item);
        intent.putExtra(KEY_CATEGORY, category);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this, false, true);
        super.onCreate(savedInstanceState);
        Feed feed = (Feed) getIntent().getSerializableExtra(KEY_FEED);
        String category = getIntent().getStringExtra(KEY_CATEGORY);
        if (feed == null) {
            finish();
            return;
        }
        ViewHandler viewHandler = null;
        if (feed.itemType == Feed.TYPE_IMAGE) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }
        viewHandler.bindInitData(feed);
    }
}