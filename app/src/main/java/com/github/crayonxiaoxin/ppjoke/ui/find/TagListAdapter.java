package com.github.crayonxiaoxin.ppjoke.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.libcommon.extension.AbsPagedListAdapter;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutTagListItemBinding;
import com.github.crayonxiaoxin.ppjoke.model.TagList;
import com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter;

public class TagListAdapter extends AbsPagedListAdapter<TagList, TagListAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId.equals(newItem.tagId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding binding = LayoutTagListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        TagList item = getItem(position);
        holder.bindData(item);
        holder.mBinding.actionFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InteractionPresenter.toggleTagLiked((LifecycleOwner) mContext, item);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutTagListItemBinding mBinding;

        public ViewHolder(LayoutTagListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bindData(TagList item) {
            mBinding.setTagList(item);
        }
    }
}
