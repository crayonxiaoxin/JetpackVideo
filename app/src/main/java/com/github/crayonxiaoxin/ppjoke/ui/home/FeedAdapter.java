package com.github.crayonxiaoxin.ppjoke.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.libcommon.extension.AbsPagedListAdapter;
import com.github.crayonxiaoxin.libcommon.extension.LiveDataBus;
import com.github.crayonxiaoxin.ppjoke.BR;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedTypeImageBinding;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedTypeVideoBinding;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter;
import com.github.crayonxiaoxin.ppjoke.ui.detail.FeedDetailActivity;
import com.github.crayonxiaoxin.ppjoke.ui.view.ListPlayerView;

public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.ViewHolder> {
    protected final LayoutInflater mInflater;
    protected String mCategory;
    protected Context mContext;

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
    protected int getItemViewType2(int position) {
        Feed item = getItem(position);
        if (item == null) return 0;
        if (item.itemType == Feed.TYPE_IMAGE) {
            return R.layout.layout_feed_type_image;
        } else if (item.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(mInflater, viewType, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Feed item = getItem(position);
        holder.bindData(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedDetailActivity.startFeedDetailActivity(mContext, item, mCategory);
                onStartFeedDetailActivity(item);
                if (mFeedObserver == null) { // ????????????????????????
                    mFeedObserver = new FeedObserver();
                    // ????????? observer??????????????????????????????
                    LiveDataBus.get().with(InteractionPresenter.DATA_FROM_INTERACTION)
                            .observe((LifecycleOwner) mContext, mFeedObserver);
                }
                mFeedObserver.setFeed(item); // ?????????????????????
            }
        });
    }

    public void onStartFeedDetailActivity(Feed feed) {

    }

    private FeedObserver mFeedObserver;

    class FeedObserver implements Observer<Feed> {

        private Feed mFeed;

        @Override
        public void onChanged(Feed newFeed) { // ?????????
            if (!mFeed.id.equals(newFeed.id)) { // ??????????????????????????????????????????????????????????????????
                return;
            }
            mFeed.ugc = newFeed.ugc;
            mFeed.author = newFeed.author;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) { // ?????????
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
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
            } else if (mBinding instanceof LayoutFeedTypeVideoBinding) {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
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
