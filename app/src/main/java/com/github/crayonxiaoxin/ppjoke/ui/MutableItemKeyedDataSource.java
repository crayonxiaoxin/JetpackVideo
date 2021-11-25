package com.github.crayonxiaoxin.ppjoke.ui;

import android.annotation.SuppressLint;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MutableItemKeyedDataSource<Key, Value> extends ItemKeyedDataSource<Key, Value> {
    private ItemKeyedDataSource mDataSource;
    public List<Value> data = new ArrayList<>();

    @SuppressLint("RestrictedApi")
    public PagedList<Value> buildNewPagedList(PagedList.Config config) {
        return new PagedList.Builder<Key, Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
    }

    public MutableItemKeyedDataSource(ItemKeyedDataSource dataSource) {
        mDataSource = dataSource;
    }

    @NonNull
    @Override
    public abstract Key getKey(@NonNull Value value);

    @Override
    public void loadAfter(@NonNull LoadParams<Key> loadParams, @NonNull LoadCallback<Value> loadCallback) {
        if (mDataSource != null) {
            //一旦 和当前DataSource关联的PagedList被提交到PagedListAdapter。那么ViewModel中创建的DataSource 就不会再被调用了
            //我们需要在分页的时候 代理一下 原来的DataSource，迫使其继续工作
            mDataSource.loadAfter(loadParams, loadCallback);
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> loadParams, @NonNull LoadCallback<Value> loadCallback) {
        loadCallback.onResult(Collections.emptyList());
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> loadInitialParams, @NonNull LoadInitialCallback<Value> loadInitialCallback) {
        loadInitialCallback.onResult(data);
    }
}
