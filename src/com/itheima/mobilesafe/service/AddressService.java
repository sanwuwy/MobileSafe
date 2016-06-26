package com.itheima.mobilesafe.service;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.dao.NumberAddressQueryUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class AddressService extends Service {

    public static final String TAG = "AddressService";
    private TelephonyManager tm;
    private MyListener listener;
    private OutCallReceiver receiver;

    // 窗体管理者
    private WindowManager wm;
    private View view;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
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
                myToast(address);
                break;
            case TelephonyManager.CALL_STATE_IDLE: // 手机空闲状态
                if (view != null) {
                    wm.removeView(view);
                    view = null;
                }
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
            myToast(address);
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

    /**
     * 自定义土司
     *
     * @param address
     */
    public void myToast(String address) {
        view = View.inflate(this, R.layout.address_show, null);
        TextView textview = (TextView) view.findViewById(R.id.tv_address);

        // "半透明","活力橙","卫士蓝","金属灰","苹果绿"
        int[] ids = { R.drawable.call_locate_white, R.drawable.call_locate_orange, R.drawable.call_locate_blue,
                R.drawable.call_locate_gray, R.drawable.call_locate_green };
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        view.setBackgroundResource(ids[sp.getInt("which", 0)]);
        textview.setText(address);
        // 窗体的参数就设置好了
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        wm.addView(view, params);
    }
}
