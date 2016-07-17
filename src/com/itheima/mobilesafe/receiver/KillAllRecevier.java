package com.itheima.mobilesafe.receiver;

import com.itheima.mobilesafe.utils.SystemInfoUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KillAllRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("自定义的广播消息接收到了..");
        SystemInfoUtils.killAll(context);
    }

}
