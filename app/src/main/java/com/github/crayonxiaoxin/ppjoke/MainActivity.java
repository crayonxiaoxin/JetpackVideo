package com.github.crayonxiaoxin.ppjoke;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.GetRequest;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.libnetwork.PostRequest;
import com.github.crayonxiaoxin.ppjoke.utils.NavGraphBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.crayonxiaoxin.ppjoke.databinding.ActivityMainBinding;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = NavHostFragment.findNavController(fragment);
//        NavigationUI.setupWithNavController(navView, navController);

        NavGraphBuilder.build(navController, this, fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);

        GetRequest<JSONObject> request = new GetRequest<>("www.imooc.com");
        request.execute();

        request.execute(new JsonCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                super.onSuccess(response);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navController.navigate(item.getItemId());
        return TextUtils.isEmpty(item.getTitle());
    }

}