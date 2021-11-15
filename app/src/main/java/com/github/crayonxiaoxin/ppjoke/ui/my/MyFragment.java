package com.github.crayonxiaoxin.ppjoke.ui.my;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.github.crayonxiaoxin.ppjoke.databinding.FragmentMyBinding;

@FragmentDestination(pageUrl = "main/tabs/my")
public class MyFragment extends Fragment {

    private FragmentMyBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.e("MyFragment", "onCreateView: ");

        binding = FragmentMyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}