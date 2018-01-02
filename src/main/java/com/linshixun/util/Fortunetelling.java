package com.linshixun.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fortunetelling {

    static String url = "http://www.zgjm.org/sm/zgss.php?type=6&act=ok&name1=";

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }


    public static void main(String[] args) throws IOException {
        Pattern compile = Pattern.compile("[\\S\\s]*?测字([\\S\\s]{3,})");
        Matcher matcher = compile.matcher("ffa测字 王重".trim());
        if (matcher.find()) {
            System.out.println(matcher.group(1));
        }
        String param = "牛粪饼";
        System.out.println(getAnser(param));
    }

    public static String getAnser(String param) throws IOException {
        if (isChineseString(param)) {
            String getURL = url + param;

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

            Document parse = Jsoup.parse(sb.toString());
            Elements select = parse.select("div[class=divine_test clearfix]");
            Elements select1 = parse.select("p[class=indent]");
            //            + "\n" + select1.text()
            String s = select.text() + "\n" + select1.text();

            return s;
        }
        return null;
    }

    private static boolean isChineseString(String param) {
        for (char c : param.toCharArray()) {
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }
}
