package com.jiang.simbot.utils;



import okhttp3.*;

import java.io.IOException;

public class ReciteRequestUtil {
    final private static String url = "http://cssdz.com:9180/";
    //发送单词的掌握状态
    public static String sendStatus(String id,String no,String status) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"id\":\""+id+"\",\r\n    \"no\":\""+no+"\",\r\n    \"status\":\""+status+"\"\r\n}");
        Request request = new Request.Builder()
                .url(url+"api/v1/update_user")
                .method("POST", body)
                .addHeader("Authorization", "cs7949335038094359bb67a16f1e1808fdbde8e256sdz")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    //注册
    public static String userRegist(String id,String name,int num) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"id\":\""+id+"\",\r\n    \"name\":\""+name+"\",\r\n    \"num\":\""+num+"\"\r\n}");
        Request request = new Request.Builder()
                .url(url+"api/v1/register")
                .method("POST", body)
                .addHeader("Authorization", "cs7949335038094359bb67a16f1e1808fdbde8e256sdz")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    //获得用户信息的请求
    public static String getUserRecordS(String id) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"id\": \""+id+"\"\r\n}");
        Request request = new Request.Builder()
                .url(url+"api/v1/user_info")
                .method("POST", body)
                .addHeader("Authorization", "cs7949335038094359bb67a16f1e1808fdbde8e256sdz")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    //获取单词的请求
    public static String getWords(String id ,String type,String num) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"id\":\""+id+"\",\r\n    \"type\":\""+type+"\",\r\n    \"num\":\""+num+"\"\r\n}");
        Request request = new Request.Builder()
                .url(url+"api/v1/get_words")
                .method("POST", body)
                .addHeader("Authorization", "cs7949335038094359bb67a16f1e1808fdbde8e256sdz")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    //记录单词
    public static String recordWord(String id,String word, String type) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"id\":\""+id+"\",\r\n    \"word\":\""+word+"\",\r\n    \"type\":\""+type+"\"\r\n}");
        Request request = new Request.Builder()
                .url(url+"api/v1/record_word")
                .method("POST", body)
                .addHeader("Authorization", "cs7949335038094359bb67a16f1e1808fdbde8e256sdz")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}