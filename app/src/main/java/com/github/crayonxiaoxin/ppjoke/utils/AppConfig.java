package com.github.crayonxiaoxin.ppjoke.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libcommon.global.AppGlobals;
import com.github.crayonxiaoxin.ppjoke.model.BottomBar;
import com.github.crayonxiaoxin.ppjoke.model.Destination;
import com.github.crayonxiaoxin.ppjoke.model.SofaTab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;

public class AppConfig {
    private static HashMap<String, Destination> sDestinationConfig;
    private static BottomBar sBottomBar;
    private static SofaTab sSofaTab;
    private static SofaTab sFindTab;

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestinationConfig == null) {
            String content = parseFile("destination.json");
            sDestinationConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
            }.getType());
        }
        return sDestinationConfig;
    }

    public static BottomBar getBottomBarConfig() {
        if (sBottomBar == null) {
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBar;
    }

    public static SofaTab getSofaTabConfig() {
        if (sSofaTab == null) {
            String content = parseFile("sofa_tabs_config.json");
            sSofaTab = JSON.parseObject(content, SofaTab.class);
            // 排序
            Collections.sort(sSofaTab.tabs, (tab1, tab2) -> tab1.index < tab2.index ? -1 : 1);
        }
        return sSofaTab;
    }

    public static SofaTab getFindTabConfig() {
        if (sFindTab == null) {
            String content = parseFile("find_tabs_config.json");
            sFindTab = JSON.parseObject(content, SofaTab.class);
            // 排序
            Collections.sort(sFindTab.tabs, (tab1, tab2) -> tab1.index < tab2.index ? -1 : 1);
        }
        return sFindTab;
    }

    private static String parseFile(String filename) {
        AssetManager assetManager = AppGlobals.getApplication().getResources().getAssets();
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            inputStream = assetManager.open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
