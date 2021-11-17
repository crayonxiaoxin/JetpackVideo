package com.github.crayonxiaoxin.ppjoke.model;


import java.util.Objects;

public class Ugc {
    public Integer likeCount;
    public Integer shareCount;
    public Integer commentCount;
    public Boolean hasFavorite;
    public Boolean hasLiked;
    public Boolean hasdiss;
    public Boolean hasDissed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ugc ugc = (Ugc) o;
        return Objects.equals(likeCount, ugc.likeCount) && Objects.equals(shareCount, ugc.shareCount) && Objects.equals(commentCount, ugc.commentCount) && Objects.equals(hasFavorite, ugc.hasFavorite) && Objects.equals(hasLiked, ugc.hasLiked) && Objects.equals(hasdiss, ugc.hasdiss) && Objects.equals(hasDissed, ugc.hasDissed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(likeCount, shareCount, commentCount, hasFavorite, hasLiked, hasdiss, hasDissed);
    }
}
