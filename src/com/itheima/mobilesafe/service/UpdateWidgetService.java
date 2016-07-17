package com.itheima.mobilesafe.service;

import java.util.Timer;
import java.util.TimerTask;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.receiver.MyWidget;
import com.itheima.mobilesafe.utils.SystemInfoUtils;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
    private ScreenOffReceiver offreceiver;
    private ScreenOnReceiver onreceiver;

    protected static final String TAG = "UpdateWidgetService";
    private Timer timer;
    private TimerTask task;
    /**
     * widget的管理器
     */
    private AppWidgetManager awm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ScreenOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("UpdateWidgetService", "屏幕锁屏了。。。");
            stopTimer();
        }
    }

    private class ScreenOnReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("UpdateWidgetService", "屏幕解锁了。。。");
            startTimer();
        }
    }

    @Override
    public void onCreate() {
        onreceiver = new ScreenOnReceiver();
        offreceiver = new ScreenOffReceiver();
        registerReceiver(onreceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(offreceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        awm = AppWidgetManager.getInstance(this);
        startTimer();
        super.onCreate();
    }

    private void startTimer() {
        if (timer == null && task == null) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "更新widget");
                    // 设置更新的组件
                    ComponentName provider = new ComponentName(UpdateWidgetService.this, MyWidget.class);
                    // 告诉桌面应用程序需要更新的widget的布局文件在本应用程序中的位置
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.process_widget);
                    // 桌面应用程序通过反射修改控件的显示内容
                    views.setTextViewText(R.id.process_count,
                            "正在运行的进程:" + SystemInfoUtils.getRunningProcessCount(getApplicationContext()) + "个");
                    long size = SystemInfoUtils.getAvailMem(getApplicationContext());
                    views.setTextViewText(R.id.process_memory,
                            "可用内存:" + Formatter.formatFileSize(getApplicationContext(), size));
                    // 描述一个动作,这个动作是由另外的一个应用程序执行的.
                    // 自定义一个广播事件,杀死后台进程的事件
                    Intent intent = new Intent();
                    intent.setAction("com.itheima.mobilesafe.killall");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);
                    awm.updateAppWidget(provider, views);
                }
            };
            // 每隔3s,执行一下task中的run方法
            timer.schedule(task, 0, 3000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(offreceiver);
        unregisterReceiver(onreceiver);
        offreceiver = null;
        onreceiver = null;
        stopTimer();
    }

    private void stopTimer() {
        if (timer != null && task != null) {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
    }
}
