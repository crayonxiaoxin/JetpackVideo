package com.github.crayonxiaoxin.ppjoke.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.github.crayonxiaoxin.ppjoke.model.SofaTab;
import com.github.crayonxiaoxin.ppjoke.ui.sofa.SofaFragment;
import com.github.crayonxiaoxin.ppjoke.utils.AppConfig;

@FragmentDestination(pageUrl = "main/tabs/find")
public class FindFragment extends SofaFragment {

    @Override
    protected Fragment getTabFragment(int position) {
        return TagListFragment.newInstance(getTabConfig().tabs.get(position).tag);
    }

    @Override
    protected SofaTab getTabConfig() {
        return AppConfig.getFindTabConfig();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().addFragmentOnAttachListener(new FragmentOnAttachListener() {
            @Override
            public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
                String tagType = fragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
                if (TextUtils.equals(tagType, "onlyFollow")) {
                    TagListViewModel tagListViewModel = new ViewModelProvider(fragment).get(TagListViewModel.class);
                    tagListViewModel.getSwitchTabLiveData().observe((LifecycleOwner) getContext(), new Observer() {
                        @Override
                        public void onChanged(Object o) {
                            viewPager.setCurrentItem(1);
                        }
                    });
                }
            }
        });
    }
}