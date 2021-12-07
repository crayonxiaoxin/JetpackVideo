package com.github.crayonxiaoxin.ppjoke.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.model.TagList;
import com.github.crayonxiaoxin.ppjoke.ui.AbsListFragment;
import com.github.crayonxiaoxin.ppjoke.ui.MutableItemKeyedDataSource;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.List;

public class TagListFragment extends AbsListFragment<TagList, TagListViewModel> {
    public static final String KEY_TAG_TYPE = "tag_type";
    private String tagType;

    public static TagListFragment newInstance(String tagType) {
        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        TagListFragment fragment = new TagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        if (TextUtils.equals(tagType, "onlyFollow")) {
            binding.emptyView.setTitle(getString(R.string.tag_list_no_follow));
            binding.emptyView.setButton(getString(R.string.tag_list_no_follow_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewModel.getSwitchTabLiveData().postValue(new Object());
                }
            });
        }
    }

    @Override
    public PagedListAdapter getAdapter() {
        tagType = getArguments().getString(KEY_TAG_TYPE);
        mViewModel.setTagType(tagType);
        return new TagListAdapter(getContext());
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        PagedList<TagList> currentList = adapter.getCurrentList();
        long tagId = currentList == null ? 0 : currentList.get(currentList.size() - 1).tagId;
        mViewModel.loadData(tagId, new ItemKeyedDataSource.LoadCallback<TagList>() {
            @Override
            public void onResult(@NonNull List<? extends TagList> list) {
                MutableItemKeyedDataSource<Long, TagList> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Long, TagList>((ItemKeyedDataSource) mViewModel.getDataSource()) {
                    @NonNull
                    @Override
                    public Long getKey(@NonNull TagList tagList) {
                        return tagList.tagId;
                    }
                };
                mutableItemKeyedDataSource.data.addAll(currentList);
                mutableItemKeyedDataSource.data.addAll(list);
                PagedList<TagList> tagLists = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
                if (tagLists.size() > 0) {
                    submitList(tagLists);
                }
            }
        });
    }
}
