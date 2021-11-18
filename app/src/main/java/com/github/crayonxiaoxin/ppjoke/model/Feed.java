package com.github.crayonxiaoxin.ppjoke.model;

import java.io.Serializable;
import java.util.Objects;

public class Feed implements Serializable {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    public Integer id;
    public Long itemId;
    public Integer itemType;
    public Long createTime;
    public Double duration;
    public String feedsText;
    public Integer authorId;
    public String activityIcon;
    public String activityText;
    public Integer width;
    public Integer height;
    public String url;
    public String cover;
    public User author;
    public Comment topComment;
    public Ugc ugc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return Objects.equals(id, feed.id) && Objects.equals(itemId, feed.itemId) && Objects.equals(itemType, feed.itemType) && Objects.equals(createTime, feed.createTime) && Objects.equals(duration, feed.duration) && Objects.equals(feedsText, feed.feedsText) && Objects.equals(authorId, feed.authorId) && Objects.equals(activityIcon, feed.activityIcon) && Objects.equals(activityText, feed.activityText) && Objects.equals(width, feed.width) && Objects.equals(height, feed.height) && Objects.equals(url, feed.url) && Objects.equals(cover, feed.cover) && Objects.equals(author, feed.author) && Objects.equals(topComment, feed.topComment) && Objects.equals(ugc, feed.ugc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, itemType, createTime, duration, feedsText, authorId, activityIcon, activityText, width, height, url, cover, author, topComment, ugc);
    }
}
