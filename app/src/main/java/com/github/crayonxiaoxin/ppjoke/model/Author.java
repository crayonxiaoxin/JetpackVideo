package com.github.crayonxiaoxin.ppjoke.model;

import java.io.Serializable;
import java.util.Objects;

public class Author implements Serializable {
    public Integer id;
    public Integer userId;
    public String name;
    public String avatar;
    public String description;
    public Integer likeCount;
    public Integer topCommentCount;
    public Integer followCount;
    public Integer followerCount;
    public String qqOpenId;
    public Long expiresTime;
    public Integer score;
    public Integer historyCount;
    public Integer commentCount;
    public Integer favoriteCount;
    public Integer feedCount;
    public Boolean hasFollow;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id) && Objects.equals(userId, author.userId) && Objects.equals(name, author.name) && Objects.equals(avatar, author.avatar) && Objects.equals(description, author.description) && Objects.equals(likeCount, author.likeCount) && Objects.equals(topCommentCount, author.topCommentCount) && Objects.equals(followCount, author.followCount) && Objects.equals(followerCount, author.followerCount) && Objects.equals(qqOpenId, author.qqOpenId) && Objects.equals(expiresTime, author.expiresTime) && Objects.equals(score, author.score) && Objects.equals(historyCount, author.historyCount) && Objects.equals(commentCount, author.commentCount) && Objects.equals(favoriteCount, author.favoriteCount) && Objects.equals(feedCount, author.feedCount) && Objects.equals(hasFollow, author.hasFollow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, avatar, description, likeCount, topCommentCount, followCount, followerCount, qqOpenId, expiresTime, score, historyCount, commentCount, favoriteCount, feedCount, hasFollow);
    }
}
