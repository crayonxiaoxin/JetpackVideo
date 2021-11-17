package com.github.crayonxiaoxin.ppjoke.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.libnetwork.Request;
import com.github.crayonxiaoxin.ppjoke.AbsViewModal;
import com.github.crayonxiaoxin.ppjoke.model.Feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AbsViewModal<Feed> {

    private volatile boolean withCache = true;

    @Override
    public DataSource createDataSource() {
        return null;
    }

    ItemKeyedDataSource<Integer, Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> loadInitialParams, @NonNull LoadInitialCallback<Feed> loadInitialCallback) {
            // 加载初始化数据
            loadData(0, loadInitialCallback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> loadParams, @NonNull LoadCallback<Feed> loadCallback) {
            // 加载分页数据
            loadData(loadParams.key, loadCallback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> loadParams, @NonNull LoadCallback<Feed> loadCallback) {
            // 向前加载数据 例如初始化加载的是第3页，此时可以向前加载第2页
            loadCallback.onResult(new ArrayList<>());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed feed) {
            return feed.id;
        }
    };

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType())
                .addParam("feedType", null)
                .addParam("userId", 0)
                .addParam("feedId", 0)
                .addParam("pageCount", 10);
        if (withCache) {
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void cacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.e("TAG", "cacheSuccess: " + response.body.size());
                }
            });
        }
        try {
            Request netRequest = withCache ? request.clone() : request;
            netRequest.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(data);
            if (key > 0) {
                // 通过 livedata 告诉ui是否关闭上拉加载动画
                getBoundaryPageData().postValue(data.size() > 0);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}