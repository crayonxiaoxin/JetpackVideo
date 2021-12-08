package com.github.crayonxiaoxin.ppjoke.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.github.crayonxiaoxin.ppjoke.BR;

import java.io.Serializable;
import java.util.Objects;

public class TagList extends BaseObservable implements Serializable {
    public Integer id;
    public String icon;
    public String background;
    public String activityIcon;
    public String title;
    public String intro;
    public Integer feedNum;
    public Long tagId;
    public Integer enterNum;
    public Integer followNum;
    public Boolean hasFollow;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagList tagList = (TagList) o;
        return Objects.equals(id, tagList.id) && Objects.equals(icon, tagList.icon) && Objects.equals(background, tagList.background) && Objects.equals(activityIcon, tagList.activityIcon) && Objects.equals(title, tagList.title) && Objects.equals(intro, tagList.intro) && Objects.equals(feedNum, tagList.feedNum) && Objects.equals(tagId, tagList.tagId) && Objects.equals(enterNum, tagList.enterNum) && Objects.equals(followNum, tagList.followNum) && Objects.equals(hasFollow, tagList.hasFollow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, icon, background, activityIcon, title, intro, feedNum, tagId, enterNum, followNum, hasFollow);
    }

    @Bindable
    public Boolean getHasFollow() {
        return hasFollow;
    }

    public void setHasFollowed(boolean hasFollowed) {
        this.hasFollow = hasFollowed;
        notifyPropertyChanged(BR._all);
    }
}
