package com.github.crayonxiaoxin.ppjoke.model;

import java.util.List;

public class BottomBar {
    public String activeColor;
    public String inActiveColor;
    public Integer selectTab;
    public List<Tabs> tabs;

    public static class Tabs {
        public Integer size;
        public Boolean enable;
        public Integer index;
        public String pageUrl;
        public String title;
        public String tintColor;
    }
}
