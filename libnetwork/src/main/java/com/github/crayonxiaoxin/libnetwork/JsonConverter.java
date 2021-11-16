package com.github.crayonxiaoxin.libnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

public class JsonConverter implements Converter {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        // 接口中第一个data固定为object
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            // 第二个data可能为object/array
            Object data1 = data.get("data");
            return JSON.parseObject(data1.toString(), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clazz) {
        JSONObject jsonObject = JSON.parseObject(response);
        // 接口中第一个data固定为object
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            // 第二个data可能为object/array
            Object data1 = data.get("data");
            return JSON.parseObject(data1.toString(), clazz);
        }
        return null;
    }
}
