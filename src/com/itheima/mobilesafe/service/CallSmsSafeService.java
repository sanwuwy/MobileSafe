package com.itheima.mobilesafe.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.itheima.mobilesafe.db.dao.BlackNumberDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
                    Log.i(TAG, "挂断电话。。。。");
                    endCall();
                }
                break;
            }
        }
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
