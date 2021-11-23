package com.github.crayonxiaoxin.ppjoke.model;

import java.util.List;

public class SofaTab {
    public Integer activeSize;
    public Integer normalSize;
    public String activeColor;
    public String normalColor;
    public Integer select;
    public Integer tabGravity;
    public List<Tab> tabs;

    public class Tab {
        public String title;
        public Integer index;
        public String tag;
        public Boolean enable;
    }

}
