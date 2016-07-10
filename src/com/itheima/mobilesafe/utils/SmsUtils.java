package com.itheima.mobilesafe.utils;

import java.io.File;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

/**
 * 短信的工具类
 *
 */
public class SmsUtils {

    /**
     * 备份短信的回调接口
     */
    public interface BackUpCallBack {
        /**
         * 开始备份的时候，设置进度的最大值
         *
         * @param max
         *            总进度
         */
        public void beforeBackup(int max);

        /**
         * 备份过程中，增加进度
         *
         * @param progress
         *            当前进度
         */
        public void onSmsBackup(int progress);

    }

    /**
     * 备份用户的短信
     *
     * @param context
     *            上下文
     * @param BackUpCallBack
     *            备份短信的接口
     */
    public static void backupSms(Context context, BackUpCallBack callBack) throws Exception {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "backup.xml");
        FileOutputStream fos = new FileOutputStream(file);
        // 把用户的短信一条一条读出来，按照一定的格式写到文件里
        XmlSerializer serializer = Xml.newSerializer();// 获取xml文件的生成器（序列化器）
        // 初始化生成器
        serializer.setOutput(fos, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag(null, "smss");
        Uri uri = Uri.parse("content://sms/");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[] { "address", "body", "type", "date" }, null, null, null);
        // 开始备份的时候，设置进度条的最大值
        int max = cursor.getCount();
        // pb.setMax(max);
        // pd.setMax(max);
        callBack.beforeBackup(max);
        serializer.attribute(null, "max", max + "");
        int process = 0;
        while (cursor.moveToNext()) {
            Thread.sleep(500);
            String address = cursor.getString(0);
            String body = cursor.getString(1);
            String type = cursor.getString(2);
            String date = cursor.getString(3);
            serializer.startTag(null, "sms");

            serializer.startTag(null, "address");
            serializer.text(address);
            serializer.endTag(null, "address");

            serializer.startTag(null, "body");
            serializer.text(body);
            serializer.endTag(null, "body");

            serializer.startTag(null, "type");
            serializer.text(type);
            serializer.endTag(null, "type");

            serializer.startTag(null, "date");
            serializer.text(date);
            serializer.endTag(null, "date");

            serializer.endTag(null, "sms");
            // 备份过程中，增加进度
            process++;
            // pb.setProgress(process);
            // pd.setProgress(process);
            callBack.onSmsBackup(process);
        }
        cursor.close();
        serializer.endTag(null, "smss");
        serializer.endDocument();
        fos.close();
    }

    /**
     * 还原短信
     *
     * @param context
     * @param flag
     *            是否清理原来的短信
     */
    public static void restoreSms(Context context, boolean flag) {
        Uri uri = Uri.parse("content://sms/");
        if (flag) {
            context.getContentResolver().delete(uri, null, null);
        }
        // 1.读取sd卡上的xml文件
        // Xml.newPullParser();

        // 2.读取max

        // 3.读取每一条短信信息，body date type address

        // 4.把短信插入到系统短息应用。

        ContentValues values = new ContentValues();
        values.put("address", "5558");
        values.put("body", "wo shi duan xin de nei rong");
        values.put("type", "1");
        values.put("date", "1395045035573");
        context.getContentResolver().insert(uri, values);
    }
}
