package com.itheima.mobilesafe.service;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.dao.NumberAddressQueryUtils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

public class AddressService extends Service {

    public static final String TAG = "AddressService";
    private TelephonyManager tm;
    private MyListener listener;
    private OutCallReceiver receiver;

    private WindowManager.LayoutParams params;
    private SharedPreferences sp;

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
        final long[] mHits = new long[2];
        view = View.inflate(this, R.layout.address_show, null);
        TextView textview = (TextView) view.findViewById(R.id.tv_address);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 双击事件
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    // 双击居中了。。。
                    params.x = wm.getDefaultDisplay().getWidth() / 2 - view.getWidth() / 2;
                    wm.updateViewLayout(view, params);
                    Editor editor = sp.edit();
                    editor.putInt("lastx", params.x);
                    editor.commit();
                }
            }
        });

        // 给view对象设置一个触摸的监听器
        view.setOnTouchListener(new OnTouchListener() {
            // 定义手指的初始化位置
            int startX;
            int startY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                    Log.i(TAG, "手指摸到控件");
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
                    Log.i(TAG, "手指在控件上移动");
                    int newX = (int) event.getRawX();
                    int newY = (int) event.getRawY();
                    int dx = newX - startX;
                    int dy = newY - startY;
                    params.x += dx;
                    params.y += dy;
                    // 考虑边界问题
                    if (params.x < 0) {
                        params.x = 0;
                    }
                    if (params.y < 0) {
                        params.y = 0;
                    }
                    if (params.x > (wm.getDefaultDisplay().getWidth() - view.getWidth())) {
                        params.x = (wm.getDefaultDisplay().getWidth() - view.getWidth());
                    }
                    if (params.y > (wm.getDefaultDisplay().getHeight() - view.getHeight())) {
                        params.y = (wm.getDefaultDisplay().getHeight() - view.getHeight());
                    }
                    wm.updateViewLayout(view, params);
                    // 重新初始化手指的开始结束位置。
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
                    // 记录控件距离屏幕左上角的坐标
                    Log.i(TAG, "手指离开控件");
                    Editor editor = sp.edit();
                    editor.putInt("lastx", params.x);
                    editor.putInt("lasty", params.y);
                    editor.commit();
                    break;
                }
                return false; // 返回true，表示事件已经处理完了，返回false，表示事件还没有处理完
            }

        });

        // "半透明","活力橙","卫士蓝","金属灰","苹果绿"
        int[] ids = { R.drawable.call_locate_white, R.drawable.call_locate_orange, R.drawable.call_locate_blue,
                R.drawable.call_locate_gray, R.drawable.call_locate_green };
        sp = getSharedPreferences("config", MODE_PRIVATE);
        view.setBackgroundResource(ids[sp.getInt("which", 0)]);
        textview.setText(address);
        // 窗体的参数就设置好了
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // 与窗体左上角对齐
        params.gravity = Gravity.TOP + Gravity.LEFT;
        // // 指定窗体距离左边100 上边100个像素，此时view.getWidth() =0 view.getHeight() =0
        params.x = sp.getInt("lastx", (wm.getDefaultDisplay().getWidth() / 2 - view.getWidth() / 2));
        params.y = sp.getInt("lasty", (wm.getDefaultDisplay().getHeight() / 2 - view.getHeight() / 2));
        Log.i(TAG, "params.x = " + params.x + ", params.y" + params.y);
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        // android系统里面具有电话优先级的一种窗体类型，记得添加权限。
        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        wm.addView(view, params);
    }
}
