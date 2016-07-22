package com.itheima.mobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * 应用程序信息的业务bean
 */
public class AppInfo {
    /**
     * 应用程序的图标
     */
    private Drawable icon;
    /**
     * 应用程序名称
     */
    private String name;
    /**
     * 应用程序的包名
     */
    private String packname;
    /**
     * 应用程序的安装位置: false 手机内存 true 外部存储
     */
    private boolean inRom;
    /**
     * 是否是系统应用 : false 系统应用 true 用户应用
     */
    private boolean userApp;

    private String receivername;

    public String getReceivername() {
        return receivername;
    }

    public void setReceivername(String receivername) {
        this.receivername = receivername;
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

    public String getPackname() {
        return packname;
    }

    public void setPackname(String packname) {
        this.packname = packname;
    }

    public boolean isInRom() {
        return inRom;
    }

    public void setInRom(boolean inRom) {
        this.inRom = inRom;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }

    @Override
    public String toString() {
        return "AppInfo [name=" + name + ", packname=" + packname + ", inRom=" + inRom + ", userApp=" + userApp + "]";
    }
}
