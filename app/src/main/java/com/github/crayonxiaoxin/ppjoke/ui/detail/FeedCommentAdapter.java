package com.github.crayonxiaoxin.ppjoke.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.crayonxiaoxin.libcommon.extension.AbsPagedListAdapter;
import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutFeedCommentListItemBinding;
import com.github.crayonxiaoxin.ppjoke.model.Comment;
import com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter;
import com.github.crayonxiaoxin.ppjoke.ui.MutableItemKeyedDataSource;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;
import com.github.crayonxiaoxin.ppjoke.ui.publish.PreviewActivity;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {
    private Context mContext;

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
    }

    @Override
    protected int getItemViewType2(int position) {
        return getItem(position).commentType;
    }

    @Override
    protected FeedCommentAdapter.ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(FeedCommentAdapter.ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        holder.mBinding.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.commentId).observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        if (success) {
                            MutableItemKeyedDataSource<Integer, Comment> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {

                                @NonNull
                                @Override
                                public Integer getKey(@NonNull Comment comment) {
                                    return comment.id;
                                }
                            };
                            PagedList<Comment> currentList = getCurrentList();
                            for (Comment comment : currentList) {
                                if (!comment.id.equals(item.id)) {
                                    mutableItemKeyedDataSource.data.add(comment);
                                }
                            }
                            PagedList<Comment> comments = mutableItemKeyedDataSource.buildNewPagedList(getCurrentList().getConfig());
                            submitList(comments);
                        }
                    }
                });
            }
        });
        holder.mBinding.commentCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isVideo = item.commentType == Comment.COMMENT_TYPE_VIDEO;
                Intent intent = PreviewActivity.intentActivityForResult((Activity) mContext, Uri.parse(isVideo ? item.videoUrl : item.imageUrl), isVideo, null);
                mContext.startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment comment) {
            mBinding.setComment(comment);
            mBinding.setOwner((LifecycleOwner) mContext);
            boolean isAuthor = UserManager.get().getUserId() == comment.userId;
            mBinding.labelAuthor.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(comment.imageUrl)) {
                mBinding.commentCover.setVisibility(View.VISIBLE);
                int dp200 = PixUtils.dp2px(200);
                mBinding.commentCover.bindData(comment.width, comment.height, 0, dp200, dp200, comment.imageUrl);
                if (!TextUtils.isEmpty(comment.videoUrl)) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
            }
        }
    }
}
