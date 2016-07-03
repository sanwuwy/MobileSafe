package com.itheima.mobilesafe.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.itheima.mobilesafe.db.dao.BlackNumberDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallSmsSafeService extends Service {

    public static final String TAG = "CallSmsSafeService";
    private MySmsReceiver receiver;
    private BlackNumberDao dao;
    private TelephonyManager tm;
    private MyCallListener listener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dao = new BlackNumberDao(this);
        receiver = new MySmsReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, filter);
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyCallListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MySmsReceiver extends BroadcastReceiver {

        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "内部广播接受者， 短信到来了");
            // 检查发件人是否是黑名单号码，设置短信拦截全部拦截。
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            for (Object object : objects) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) object);
                String sender = message.getOriginatingAddress();
                String mode = dao.findMode(sender);
                if ("2".equals(mode) || "3".equals(mode)) {
                    Log.i(TAG, "拦截短信");
                    abortBroadcast();
                }
                // 演示代码。
                String body = message.getMessageBody();
                if (body.contains("fapiao")) {
                    // 你的头发票亮的很 语言分词技术。
                    Log.i(TAG, "拦截发票短信");
                    abortBroadcast();
                }
            }
        }
    }

    private class MyCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
            case TelephonyManager.CALL_STATE_RINGING: // 响铃
                String mode = dao.findMode(incomingNumber);
                if ("1".equals(mode) || "3".equals(mode)) {
                    // 观察呼叫记录数据库内容的变化。
                    Uri uri = Uri.parse("content://call_log/calls");
                    getContentResolver().registerContentObserver(uri, true,
                            new CallLogObserver(incomingNumber, 1, new Handler()));
                    Log.i(TAG, "挂断电话。。。。");
                    endCall();// 运行在另外一个进程里面的远程服务的方法。 方法调用后，呼叫记录可能还没有生成。
                }
                break;
            }
        }
    }

    private class CallLogObserver extends ContentObserver {
        private String incomingNumber;
        private int count;

        public CallLogObserver(String incomingNumber, int count, Handler handler) {
            super(handler);
            this.incomingNumber = incomingNumber;
            this.count = count;
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "数据库的内容变化了，产生了呼叫记录");
            getContentResolver().unregisterContentObserver(this);
            deleteCallLog(incomingNumber);
            super.onChange(selfChange);
        }

    }

    /**
     * 利用内容提供者删除呼叫记录
     *
     * @param incomingNumber
     */
    public void deleteCallLog(String incomingNumber) {
        ContentResolver resolver = getContentResolver();
        // 呼叫记录uri的路径
        Uri uri = Uri.parse("content://call_log/calls");
        resolver.delete(uri, "number=?", new String[] { incomingNumber });
    }

    public void endCall() {
        // IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
        try {
            // 加载ServiceManager的字节码
            Class clazz = CallSmsSafeService.class.getClassLoader().loadClass("android.os.ServiceManager");
            Method method = clazz.getDeclaredMethod("getService", String.class);
            IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
            ITelephony.Stub.asInterface(ibinder).endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        receiver = null;
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;
        super.onDestroy();
    }
}
