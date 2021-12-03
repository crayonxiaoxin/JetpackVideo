package com.github.crayonxiaoxin.ppjoke.ui.publish;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.crayonxiaoxin.libnavannotation.ActivityDestination;
import com.github.crayonxiaoxin.ppjoke.R;

@ActivityDestination(pageUrl = "main/tabs/publish")
public class PublishActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
    }
}
