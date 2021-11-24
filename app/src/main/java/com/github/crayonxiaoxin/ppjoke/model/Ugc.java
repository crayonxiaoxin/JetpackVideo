package com.github.crayonxiaoxin.ppjoke.model;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.github.crayonxiaoxin.ppjoke.BR;

import java.io.Serializable;
import java.util.Objects;

public class Ugc extends BaseObservable implements Serializable {
    public Integer likeCount;
    public Integer shareCount;
    public Integer commentCount;
    public Boolean hasFavorite;
    public Boolean hasLiked;
    public Boolean hasdiss;
    public Boolean hasDissed;

    public Ugc() {
        likeCount = 0;
        shareCount = 0;
        commentCount = 0;
        hasFavorite = false;
        hasLiked = false;
        hasdiss = false;
        hasDissed = false;
    }

    @Bindable
    public Boolean getHasFavorite() {
        return hasFavorite;
    }

    public void setHasFavorites(Boolean hasFavorite) {
        this.hasFavorite = hasFavorite;
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public Boolean getHasLiked() {
        return hasLiked;
    }

    // 这里是手动改变 dataBinding，不能与 setter 一致 否则报错
    public void setHasLike(Boolean hasLiked) {
        if (this.hasLiked == hasLiked) return;
        if (hasLiked) {
            this.likeCount += 1;
            if (this.hasdiss) setHasdissd(false);
        } else {
            this.likeCount -= 1;
        }
        this.hasLiked = hasLiked;
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public Boolean getHasdiss() {
        return hasdiss;
    }

    public void setHasdissd(Boolean hasdiss) {
        if (this.hasdiss == hasdiss) return;
        if (hasdiss && this.hasLiked) {
            setHasLike(false);
        }
        this.hasdiss = hasdiss;
    }

    @Bindable
    public Integer getShareCount() {
        return shareCount;
    }

    public void setSharedCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

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
