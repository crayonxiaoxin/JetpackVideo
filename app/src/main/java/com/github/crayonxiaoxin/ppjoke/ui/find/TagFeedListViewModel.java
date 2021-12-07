package com.github.crayonxiaoxin.ppjoke.ui.find;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.ppjoke.AbsViewModal;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagFeedListViewModel extends AbsViewModal<Feed> {
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed feed) {
            return feed.id;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> loadParams, @NonNull LoadCallback<Feed> loadCallback) {
            loadData(loadParams.key, loadCallback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> loadParams, @NonNull LoadCallback<Feed> loadCallback) {
            loadCallback.onResult(Collections.emptyList());
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> loadInitialParams, @NonNull LoadInitialCallback<Feed> loadInitialCallback) {
            loadData(loadInitialParams.requestedInitialKey, loadInitialCallback);
        }
    }

    private void loadData(Integer requestedKey, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryHotFeedsList")
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType())
                .addParam("feedType", mFeedType)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", requestedKey)
                .addParam("pageCount", 10).execute();
        List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
        callback.onResult(result);
    }
}
