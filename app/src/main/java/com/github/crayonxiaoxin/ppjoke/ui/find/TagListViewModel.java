package com.github.crayonxiaoxin.ppjoke.ui.find;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.ppjoke.AbsViewModal;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.model.TagList;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagListViewModel extends AbsViewModal<TagList> {
    private String tagType;
    private int offset;
    private AtomicBoolean loadAfter = new AtomicBoolean(); // 防止paging和手動加載重複觸發
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    @SuppressLint("RestrictedApi")
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback<TagList> callback) {
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        } else {
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ((TagListViewModel.DataSource) getDataSource()).loadData(tagId, callback);
                }
            });
        }
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    private class DataSource extends ItemKeyedDataSource<Long, TagList> {

        @NonNull
        @Override
        public Long getKey(@NonNull TagList tagList) {
            return (Long) tagList.tagId;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> loadParams, @NonNull LoadCallback<TagList> loadCallback) {
            loadData((Long) loadParams.key, loadCallback);
        }

        private void loadData(Long key, LoadCallback<TagList> loadCallback) {
            if (key > 0) {
                loadAfter.set(true);
            }
            ApiResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .responseType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("tagId", key)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset)
                    .execute();
            List<TagList> result = (response == null || response.body == null) ? Collections.emptyList() : response.body;
            if (key > 0) {
                offset += result.size();
                getBoundaryPageData().postValue(result.size() > 0);
                loadAfter.set(false);
            } else {
                offset = result.size();
            }
            loadCallback.onResult(result);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> loadParams, @NonNull LoadCallback<TagList> loadCallback) {
            loadCallback.onResult(Collections.emptyList());
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> loadInitialParams, @NonNull LoadInitialCallback<TagList> loadInitialCallback) {
            loadData(0L, loadInitialCallback);
        }
    }
}
