package com.itheima.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamTools {

    public static String readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        byte[] result = baos.toByteArray();
        // 这里简单的处理一下网页字符编码的问题
        String temp = new String(result);
        if (temp.contains("utf-8")) {
            return new String(result);
        } else if (temp.contains("gbk")) {
            return new String(result, "gbk");
        } else if (temp.contains("gb2312")) {
            return new String(result, "gb2312");
        }
        return temp;

    }
}
