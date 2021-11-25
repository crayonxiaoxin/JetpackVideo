package com.github.crayonxiaoxin.ppjoke.ui.home;

import static com.github.crayonxiaoxin.ppjoke.BR.feed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.libcommon.extension.LiveDataBus;
import com.github.crayonxiaoxin.ppjoke.BR;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedTypeImageBinding;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedTypeVideoBinding;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter;
import com.github.crayonxiaoxin.ppjoke.ui.detail.FeedDetailActivity;
import com.github.crayonxiaoxin.ppjoke.ui.view.ListPlayerView;

class FeedAdapter extends PagedListAdapter<Feed, FeedAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private String mCategory;
    private Context mContext;

    protected FeedAdapter(Context context, String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCategory = category;
    }

    @Override
    public int getItemViewType(int position) {
        Feed item = getItem(position);
        return item.itemType;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        if (viewType == Feed.TYPE_IMAGE) {
            binding = LayoutFeedTypeImageBinding.inflate(mInflater, parent, false);
        } else {
            binding = LayoutFeedTypeVideoBinding.inflate(mInflater, parent, false);
        }
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedAdapter.ViewHolder holder, int position) {
        Feed item = getItem(position);
        holder.bindData(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedDetailActivity.startFeedDetailActivity(mContext, item, mCategory);
                if (mFeedObserver == null) { // 没有的时候才注册
                    mFeedObserver = new FeedObserver();
                    // 先注册 observer，监听详情页数据改变
                    LiveDataBus.get().with(InteractionPresenter.DATA_FROM_INTERACTION)
                            .observe((LifecycleOwner) mContext, mFeedObserver);
                }
                mFeedObserver.setFeed(item); // 每次点击的数据
            }
        });
    }

    private FeedObserver mFeedObserver;

    class FeedObserver implements Observer<Feed> {

        private Feed mFeed;

        @Override
        public void onChanged(Feed newFeed) { // 新数据
            if (!mFeed.id.equals(newFeed.id)) { // 如果新数据不是当前点击的旧数据，则不需要更新
                return;
            }
            mFeed.ugc = newFeed.ugc;
            mFeed.author = newFeed.author;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) { // 老数据
            mFeed = feed;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding mBinding;
        private ListPlayerView listPlayerView;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            mBinding.setVariable(BR.feed, item);
            mBinding.setVariable(BR.lifeCycleOwner, mContext);
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
//                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
//                imageBinding.setLifecycleOwner((LifecycleOwner) mContext); // include 中的 variable 不传递
            } else {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
//                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
//                videoBinding.setLifecycleOwner((LifecycleOwner) mContext);
                listPlayerView = videoBinding.listPlayerView;
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }
}
