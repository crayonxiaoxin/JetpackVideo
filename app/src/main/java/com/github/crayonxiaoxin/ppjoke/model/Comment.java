package com.github.crayonxiaoxin.ppjoke.model;

import java.io.Serializable;
import java.util.Objects;

public class Comment implements Serializable {
    public Integer id;
    public Integer itemId;
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
