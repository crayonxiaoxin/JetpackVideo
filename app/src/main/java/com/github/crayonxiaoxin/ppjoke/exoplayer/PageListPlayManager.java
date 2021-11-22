package com.github.crayonxiaoxin.ppjoke.exoplayer;

import android.app.Application;
import android.net.Uri;

import com.github.crayonxiaoxin.libcommon.AppGlobals;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;

public class PageListPlayManager {
    private static HashMap<String, PageListPlay> sPageListPlayHashMap = new HashMap<>();

    private static final ProgressiveMediaSource.Factory mediaSourceFactory;

    static {
        Application application = AppGlobals.getApplication();
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        dataSourceFactory.setUserAgent(Util.getUserAgent(application, application.getPackageName()));
        Cache cache = new SimpleCache(application.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 200));
        CacheDataSink.Factory cacheDataSinkFactory = new CacheDataSink.Factory().setCache(cache).setFragmentSize(Long.MAX_VALUE);
        CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory().setCache(cache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setCacheReadDataSourceFactory(new FileDataSource.Factory())
                .setCacheWriteDataSinkFactory(cacheDataSinkFactory)
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE);
        mediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    public static MediaSource createMediaSource(String url) {
        return mediaSourceFactory.createMediaSource(MediaItem.fromUri(Uri.parse(url)));
    }

    public static PageListPlay get(String pageName) {
        PageListPlay pageListPlay = sPageListPlayHashMap.get(pageName);
        if (pageListPlay == null) {
            pageListPlay = new PageListPlay();
            sPageListPlayHashMap.put(pageName, pageListPlay);
        }
        return pageListPlay;
    }

    public static void release(String pageName) {
        PageListPlay pageListPlay = sPageListPlayHashMap.get(pageName);
        if (pageListPlay != null) {
            sPageListPlayHashMap.remove(pageName);
        }
    }
}
