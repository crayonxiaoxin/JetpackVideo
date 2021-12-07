package com.github.crayonxiaoxin.ppjoke.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.FragmentSofaBinding;
import com.github.crayonxiaoxin.ppjoke.model.SofaTab;
import com.github.crayonxiaoxin.ppjoke.ui.home.HomeFragment;
import com.github.crayonxiaoxin.ppjoke.utils.AppConfig;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@FragmentDestination(pageUrl = "main/tabs/sofa")
public class SofaFragment extends Fragment {


    protected FragmentSofaBinding binding;
    protected TabLayout tabLayout;
    protected ViewPager2 viewPager;
    protected ArrayList<SofaTab.Tab> tabs;
    protected SofaTab tabConfig;
    protected Map<Integer, Fragment> fragmentMap = new HashMap<>();
    protected TabLayoutMediator tabLayoutMediator;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("SofaFragment", "onCreateView: ");
        binding = FragmentSofaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;
        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for (SofaTab.Tab tab : tabConfig.tabs) {
            if (tab.enable) {
                tabs.add(tab);
            }
        }
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return createTabFragment(position);
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });
        // tabLayout 和 viewPager 联动，注意：这里会清空所有之前的 tab，似乎 xml 中的 IndicatorColor 都会失效
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(makeTabView(position));
            }
        });
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.color_theme));
        tabLayoutMediator.attach();
        // 对选中的 tab 文字样式进行改变
        viewPager.registerOnPageChangeCallback(pageChangeCallback);
        // 默认选中
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(tabConfig.select);
            }
        });
    }

    ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {
                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] state = new int[2][];
        state[0] = new int[]{android.R.attr.state_selected};
        state[1] = new int[]{};
        int[] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};
        ColorStateList colorStateList = new ColorStateList(state, colors);
        tabView.setTextColor(colorStateList);
        tabView.setText(tabs.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    protected Fragment createTabFragment(int position) {
        Fragment fragment = fragmentMap.get(position);
        if (fragment == null) {
            fragment = getTabFragment(position);
        }
        return fragment;
    }

    protected Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).tag);
    }

    protected SofaTab getTabConfig(){
        return AppConfig.getSofaTabConfig();
    }

    @Override
    public void onDestroyView() {
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        tabLayoutMediator.detach();
        super.onDestroyView();
    }
}