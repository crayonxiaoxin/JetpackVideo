package com.github.crayonxiaoxin.ppjoke;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.github.crayonxiaoxin.ppjoke.model.Destination;
import com.github.crayonxiaoxin.ppjoke.model.User;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;
import com.github.crayonxiaoxin.ppjoke.ui.view.AppBottomBar;
import com.github.crayonxiaoxin.ppjoke.utils.AppConfig;
import com.github.crayonxiaoxin.ppjoke.utils.NavGraphBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = NavHostFragment.findNavController(fragment);
        NavigationUI.setupWithNavController(navView, navController);

        NavGraphBuilder.build(navController, this, fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        HashMap<String, Destination> dest = AppConfig.getDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = dest.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> next = iterator.next();
            Destination value = next.getValue();
            if (value != null && !UserManager.get().isLogin() && value.needLogin && value.id == item.getItemId()) {
                UserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        if (user != null) {
                            navView.setSelectedItemId(item.getItemId());
                        }
                    }
                });
                return false;
            }
        }
        navController.navigate(item.getItemId());
        return TextUtils.isEmpty(item.getTitle());
    }

}