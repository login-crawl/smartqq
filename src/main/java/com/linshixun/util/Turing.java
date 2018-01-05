package com.linshixun.util;


//tuling.java

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Turing {
    static String APIKEY = "e7a1447ed2182d57758ca845e5a0f36e";

    public static void main(String[] args) throws IOException {

        String question = "财运";//这是上传给云机器人的问题
        //String INFO = URLEncoder.encode("北京今日天气", "utf-8");
        System.out.println(question);
        String sb = getAnser(question, "发");
        System.out.println(sb);

    }

    static Gson gson = new GsonBuilder().create();

    public static String getAnser(String question, String uid) throws IOException {
        String INFO = URLEncoder.encode(question.replaceAll("[\r\n]", ""), "utf-8");
        String getURL = "http://www.tuling123.com/openapi/api?key=" + APIKEY + "&info=" + INFO + "&userid=" + uid;
        URL getUrl = new URL(getURL);
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        connection.connect();

        // 取得输入流，并使用Reader读取
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        // 断开连接
        connection.disconnect();

        HashMap hashMap = gson.fromJson(sb.toString(), HashMap.class);
        StringBuilder stringBuilder = new StringBuilder();
        if (Double.valueOf(hashMap.get("code").toString()) >= 100000d) {
            System.out.println(sb);

            mapValues(stringBuilder, hashMap);

            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            System.out.println(stringBuilder);
            //            return (String) hashMap.get("text");
            return stringBuilder.toString().replaceAll("tuling", "");
        } else {
            System.out.println(sb);
            return null;
        }

    }

    private static void mapValues(StringBuilder sb, Object o) {
        if (o instanceof String) {
            sb.append(o).append("\n");
        } else if (o instanceof Map) {
            ((Map) o).forEach((k, v) -> {
                mapValues(sb, v);
            });

        } else if (o instanceof List) {
            ((List) o).forEach(l -> {
                mapValues(sb, l);
            });
        } else {

        }
    }
}
