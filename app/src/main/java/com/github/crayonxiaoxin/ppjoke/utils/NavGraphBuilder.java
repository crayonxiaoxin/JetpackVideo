package com.github.crayonxiaoxin.ppjoke.utils;

import android.content.ComponentName;

import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavHostController;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.github.crayonxiaoxin.ppjoke.model.Destination;

import java.util.HashMap;

public class NavGraphBuilder {
    public static void build(NavController controller) {
        NavigatorProvider provider = controller.getNavigatorProvider();
        FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);

        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        for (Destination value : destConfig.values()) {
            if (value.isFragment) {
                FragmentNavigator.Destination dest = fragmentNavigator.createDestination();
                dest.setClassName(value.clazzName);
                dest.setId(value.id);
                dest.addDeepLink(value.pageUrl);
                navGraph.addDestination(dest);
            } else {
                ActivityNavigator.Destination dest = activityNavigator.createDestination();
                dest.setComponentName(new ComponentName(AppGlobals.getApplication().getPackageName(), value.clazzName));
                dest.setId(value.id);
                dest.addDeepLink(value.pageUrl);
                navGraph.addDestination(dest);
            }
            if (value.asStarter) {
                navGraph.setStartDestination(value.id);
            }
        }

        controller.setGraph(navGraph);
    }
}
