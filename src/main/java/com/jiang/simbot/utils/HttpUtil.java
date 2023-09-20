package com.jiang.simbot.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class HttpUtil {
    public static String returnResponseBody(String url){
        // 发起 GET 请求
        HttpRequest request = HttpRequest.get(url);
        HttpResponse response = request.execute();
        // 获取响应结果
        return response.body();
    }
}
