package com.itheima.mobilesafe.receiver;

import com.itheima.mobilesafe.service.UpdateWidgetService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class MyWidget extends AppWidgetProvider {

    /**
     * 每次操作widget都会被调用
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("MyWidget --> onReceive");
        Intent i = new Intent(context, UpdateWidgetService.class);
        context.startService(i);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        System.out.println("MyWidget --> onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 只在第一个widget被添加到桌面时执行一次
     */
    @Override
    public void onEnabled(Context context) {
        System.out.println("MyWidget --> onEnabled");
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.startService(intent);
        super.onEnabled(context);
    }

    /**
     * 只在最后一个widget从桌面上移除时执行一次
     */
    @Override
    public void onDisabled(Context context) {
        System.out.println("MyWidget --> onDisabled");
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }
}
