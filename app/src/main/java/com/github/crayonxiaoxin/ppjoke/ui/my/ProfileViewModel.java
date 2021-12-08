package com.github.crayonxiaoxin.ppjoke.ui.my;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.ppjoke.AbsViewModal;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.callback.Callback;

public class ProfileViewModel extends AbsViewModal<Feed> {
    private String profileType;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    private class DataSource extends ItemKeyedDataSource<Long, Feed> {

        @NonNull
        @Override
        public Long getKey(@NonNull Feed feed) {
            return feed.id.longValue();
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> loadParams, @NonNull LoadCallback<Feed> loadCallback) {
            loadData(loadParams.key, loadCallback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> loadParams, @NonNull LoadCallback<Feed> loadCallback) {
            loadCallback.onResult(Collections.emptyList());
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> loadInitialParams, @NonNull LoadInitialCallback<Feed> loadInitialCallback) {
            loadData(0L, loadInitialCallback);
        }

        private void loadData(Long key, ItemKeyedDataSource.LoadCallback<Feed> loadCallback) {
            ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryProfileFeeds")
                    .addParam("feedId", key)
                    .addParam("pageCount", 10)
                    .addParam("profileType", profileType)
                    .addParam("userId", UserManager.get().getUserId())
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();
            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            loadCallback.onResult(result);
            if (key > 0) {
                getBoundaryPageData().postValue(result.size() > 0);
            }
        }
    }

}
