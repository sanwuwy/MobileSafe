package com.itheima.mobilesafe.service;

import com.itheima.mobilesafe.db.dao.NumberAddressQueryUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class AddressService extends Service {

    public static final String TAG = "AddressService";
    private TelephonyManager tm;
    private MyListener listener;
    private OutCallReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        // 用代码去注册广播接收者
        receiver = new OutCallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(receiver, filter);
    }

    private class MyListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:// 来电铃声响起:
                // 查询数据库的操作
                String address = NumberAddressQueryUtils.queryNumber(incomingNumber);
                Toast.makeText(getApplicationContext(), address, 1).show();
                break;
            }
        }
    }

    // 服务里面的内部类
    // 广播接收者的生命周期和服务一样
    private class OutCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 这就是我们拿到的播出去的电话号码
            String phone = getResultData();
            Log.i(TAG, "拨打的电话为：" + phone);
            // 查询数据库
            String address = NumberAddressQueryUtils.queryNumber(phone);
            Toast.makeText(context, address, 0).show();
        }

    }

    @Override
    public void onDestroy() {
        // 取消监听来电
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;

        // 用代码取消注册广播接收者
        unregisterReceiver(receiver);
        receiver = null;
        super.onDestroy();
    }
}
