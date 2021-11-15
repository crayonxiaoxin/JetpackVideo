package com.github.crayonxiaoxin.ppjoke.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.model.BottomBar;
import com.github.crayonxiaoxin.ppjoke.model.Destination;
import com.github.crayonxiaoxin.ppjoke.utils.AppConfig;
import com.github.crayonxiaoxin.ppjoke.utils.AppGlobals;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class AppBottomBar extends BottomNavigationView {

    private static int[] sIcons = new int[]{R.drawable.icon_tab_home, R.drawable.icon_tab_sofa, R.drawable.icon_tab_publish, R.drawable.icon_tab_find, R.drawable.icon_tab_mine};

    public AppBottomBar(@NonNull Context context) {
        this(context, null);
    }

    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        BottomBar bottomBar = AppConfig.getBottomBar();
        List<BottomBar.Tabs> tabs = bottomBar.tabs;

        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(bottomBar.activeColor), Color.parseColor(bottomBar.inActiveColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);

        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        setLabelVisibilityMode(LABEL_VISIBILITY_LABELED);
        setSelectedItemId(bottomBar.selectTab);

        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.enable) return;
            int menuItemId = getId(tab.pageUrl);
            if (menuItemId < 0) return;
            // 每 add 一次都会清空一次再添加，用以排序
            MenuItem item = getMenu().add(0, menuItemId, tab.index, tab.title);
            item.setIcon(sIcons[tab.index]);
        }

        // 所以需要在添加之后再设置 icon 大小
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            int iconSize = dp2px(tab.size);
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.index);
            itemView.setIconSize(iconSize);
            if (TextUtils.isEmpty(tab.title)) {
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)));
                itemView.setShifting(false);
            }
        }
    }

    private int dp2px(int size) {
        float value = getContext().getResources().getDisplayMetrics().density * size + 0.5f;
        return (int) value;
    }

    private int getId(String pageUrl) {
        Destination dest = AppConfig.getDestConfig().get(pageUrl);
        if (dest == null) return -1;
        return dest.id;
    }
}
