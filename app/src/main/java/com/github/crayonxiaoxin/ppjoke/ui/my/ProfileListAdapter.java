package com.github.crayonxiaoxin.ppjoke.ui.my;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter;
import com.github.crayonxiaoxin.ppjoke.ui.MutableItemKeyedDataSource;
import com.github.crayonxiaoxin.ppjoke.ui.home.FeedAdapter;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;
import com.github.crayonxiaoxin.ppjoke.utils.TimeUtils;

public class ProfileListAdapter extends FeedAdapter {
    protected ProfileListAdapter(Context context, String category) {
        super(context, category);
    }

    @Override
    protected int getItemViewType2(int position) {
        if (TextUtils.equals(mCategory, ProfileActivity.TAB_TYPE_COMMENT)) {
            return R.layout.layout_feed_type_comment;
        } else if (TextUtils.equals(mCategory, ProfileActivity.TAB_TYPE_ALL)) {
            Feed feed = getItem(position);
            if (feed.topComment != null && feed.topComment.userId == UserManager.get().getUserId()) {
                return R.layout.layout_feed_type_comment;
            }
        }
        return super.getItemViewType2(position);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        super.onBindViewHolder2(holder, position);
        Feed item = getItem(position);
        View dissView = holder.itemView.findViewById(R.id.diss);
        View deleteView = holder.itemView.findViewById(R.id.delete);
        TextView createTimeView = holder.itemView.findViewById(R.id.create_time);
        createTimeView.setVisibility(View.VISIBLE);
        createTimeView.setText(TimeUtils.calculate(item.createTime));

        boolean isCommentTab = TextUtils.equals(mCategory, ProfileActivity.TAB_TYPE_COMMENT);
        if (isCommentTab) {
            dissView.setVisibility(View.GONE);
        }

        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCommentTab) {
                    InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.topComment.commentId)
                            .observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean success) {
                                    refreshList(item);
                                }
                            });
                } else {
                    InteractionPresenter.deleteFeed(mContext, item.itemId)
                            .observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean success) {
                                    refreshList(item);
                                }
                            });
                }
            }
        });
    }

    private void refreshList(Feed item) {
        PagedList<Feed> currentList = getCurrentList();
        MutableItemKeyedDataSource<Long, Feed> dataSource = new MutableItemKeyedDataSource<Long, Feed>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Long getKey(@NonNull Feed feed) {
                return (long) feed.id;
            }
        };
        for (Feed feed : currentList) {
            if (feed != item) {
                dataSource.data.add(feed);
            }
        }
        PagedList<Feed> feeds = dataSource.buildNewPagedList(currentList.getConfig());
        submitList(feeds);
    }
}
