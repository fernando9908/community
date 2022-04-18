package com.julymelon.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 工具类中方法均为静态，方便调用
public class CommunityUtil {

    // 生成随机字符串
    // 静态方法可直接调用
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // hello -> abc123def456
    // hello + 3e4a8 -> abc123def456abc
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        // 把以上数据封装为一个JSON对象
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        // 把JSON对象转化为字符串
        // 即转化为JSON格式的字符串
        return json.toJSONString();
    }

    // JSON格式仍属于字符串
    // 使用jQuery实现Ajax
    // Ajax中的x本来指xml，但是现在使用JSON较多
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    // code必须有，msg也比较重要，map（业务数据）有时候没有
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    // 测试JSON代码
    // 因为不需要容器注入，因此不用写测试方法
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 25);
        System.out.println(getJSONString(0, "ok", map));
        System.out.println(getJSONString(0));
    }
}
