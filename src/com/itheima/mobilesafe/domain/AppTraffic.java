package com.itheima.mobilesafe.domain;

import android.graphics.drawable.Drawable;

public class AppTraffic {
    private int uid;// 进程的uid
    /**
     * 应用程序的图标
     */
    private Drawable icon;
    private String name;// 进程 的名称
    private long uidRxBytes;// 下载数据量
    private long uidTxBytes;// 上载数据量


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUidRxBytes() {
        return uidRxBytes;
    }

    public void setUidRxBytes(long uidRxBytes) {
        this.uidRxBytes = uidRxBytes;
    }

    public long getUidTxBytes() {
        return uidTxBytes;
    }

    public void setUidTxBytes(long uidTxBytes) {
        this.uidTxBytes = uidTxBytes;
    }

}