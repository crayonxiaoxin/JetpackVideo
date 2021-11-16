package com.github.crayonxiaoxin.ppjoke.model;

public class Comment {
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
    public Author author;
    public Ugc ugc;
}
