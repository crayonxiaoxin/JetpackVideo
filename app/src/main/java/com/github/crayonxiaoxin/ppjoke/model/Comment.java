package com.github.crayonxiaoxin.ppjoke.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;
import java.util.Objects;

public class Comment extends BaseObservable implements Serializable {

    public static final int COMMENT_TYPE_VIDEO = 3;
    public static final int COMMENT_TYPE_IMAGE_TEXT = 2;

    public Integer id;
    public Long itemId;
    public Long commentId;
    public Integer userId;
    public Integer commentType;
    public Long createTime;
    public Integer commentCount;
    public Integer likeCount;
    public String commentText;
    public String imageUrl;
    public String videoUrl;
    public Integer width;
    public Integer height;
    public Boolean hasLiked;
    public User author;
    public Ugc ugc;

    @Bindable
    public Boolean getHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(Boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    @Bindable
    public Ugc getUgc() {
        if (ugc == null) ugc = new Ugc();
        return ugc;
    }

    @Bindable
    public User getAuthor() {
        if (author == null) author = new User();
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) && Objects.equals(itemId, comment.itemId) && Objects.equals(commentId, comment.commentId) && Objects.equals(userId, comment.userId) && Objects.equals(commentType, comment.commentType) && Objects.equals(createTime, comment.createTime) && Objects.equals(commentCount, comment.commentCount) && Objects.equals(likeCount, comment.likeCount) && Objects.equals(commentText, comment.commentText) && Objects.equals(imageUrl, comment.imageUrl) && Objects.equals(videoUrl, comment.videoUrl) && Objects.equals(width, comment.width) && Objects.equals(height, comment.height) && Objects.equals(hasLiked, comment.hasLiked) && Objects.equals(author, comment.author) && Objects.equals(ugc, comment.ugc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, commentId, userId, commentType, createTime, commentCount, likeCount, commentText, imageUrl, videoUrl, width, height, hasLiked, author, ugc);
    }
}
