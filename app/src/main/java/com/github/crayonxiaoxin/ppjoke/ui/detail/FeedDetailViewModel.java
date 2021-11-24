package com.github.crayonxiaoxin.ppjoke.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.ppjoke.AbsViewModal;
import com.github.crayonxiaoxin.ppjoke.model.Comment;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedDetailViewModel extends AbsViewModal<Comment> {
    private long itemId;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setItemId(long id) {
        itemId = id;
    }

    class DataSource extends ItemKeyedDataSource<Integer, Comment> {

        @NonNull
        @Override
        public Integer getKey(@NonNull Comment comment) {
            return comment.id;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> loadParams, @NonNull LoadCallback<Comment> loadCallback) {
            loadData(loadParams.key, loadParams.requestedLoadSize, loadCallback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> loadParams, @NonNull LoadCallback<Comment> loadCallback) {
            loadCallback.onResult(Collections.emptyList());
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> loadInitialParams, @NonNull LoadInitialCallback<Comment> loadInitialCallback) {
            loadData(loadInitialParams.requestedInitialKey, loadInitialParams.requestedLoadSize, loadInitialCallback);
        }

        private void loadData(Integer key, int requestedLoadSize, ItemKeyedDataSource.LoadCallback<Comment> loadCallback) {
            ApiResponse<List<Comment>> response = ApiService.get("/comment/queryFeedComments")
                    .addParam("id", key) // 评论id
                    .addParam("itemId", itemId) // 帖子id
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", requestedLoadSize)
                    .responseType(new TypeReference<ArrayList<Comment>>() {
                    }.getType())
                    .execute();
            List<Comment> list = response == null || response.body == null ? Collections.emptyList() : response.body;
            loadCallback.onResult(list);
        }
    }


}
