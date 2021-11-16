package com.github.crayonxiaoxin.ppjoke.ui.find;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.github.crayonxiaoxin.ppjoke.R;

@FragmentDestination(pageUrl = "main/tabs/find")
public class FindFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.e("FindFragment", "onCreateView: ");

        View view = inflater.inflate(R.layout.fragment_find, container, false);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}