package com.github.crayonxiaoxin.ppjoke.ui.publish;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.crayonxiaoxin.libnavannotation.ActivityDestination;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityCaptureBinding;

@ActivityDestination(pageUrl = "main/tabs/publish")
public class CaptureActivity extends AppCompatActivity {
    private ActivityCaptureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaptureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}
