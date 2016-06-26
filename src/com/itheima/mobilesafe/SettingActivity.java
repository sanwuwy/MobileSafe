package com.itheima.mobilesafe;

import com.itheima.mobilesafe.service.AddressService;
import com.itheima.mobilesafe.ui.SettingClickView;
import com.itheima.mobilesafe.ui.SettingItemView;
import com.itheima.mobilesafe.utils.ServiceUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingActivity extends Activity {
    // 设置是否检测更新
    private SettingItemView siv_update;
    // 设置是否显示号码归属地
    private SettingItemView siv_show_address;
    // 设置归属地提示框风格
    private SettingClickView scv_changebg;
    private Intent showAddress;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        siv_update = (SettingItemView) findViewById(R.id.siv_update);

        boolean update = sp.getBoolean("update", true);
        if (update) {
            // 自动升级已经开启
            siv_update.setChecked(true);
        } else {
            // 自动升级已经关闭
            siv_update.setChecked(false);
        }
        // 让整个SettingItemView获取点击事件
        siv_update.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Editor editor = sp.edit();
                // 判断是否有选中
                if (siv_update.isChecked()) { // 已经打开自动升级了
                    siv_update.setChecked(false);
                    editor.putBoolean("update", false);
                } else { // 没有打开自动升级
                    siv_update.setChecked(true);
                    editor.putBoolean("update", true);
                }
                editor.commit();
            }
        });

        siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
        siv_show_address.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (siv_show_address.isChecked()) {
                    // 变为非选中状态
                    siv_show_address.setChecked(false);
                    stopService(showAddress);
                } else {
                    siv_show_address.setChecked(true);
                    startService(showAddress);
                }
            }
        });

        scv_changebg = (SettingClickView) findViewById(R.id.scv_changebg);
        final String[] items = { "半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿" };
        int which = sp.getInt("which", 0);
        scv_changebg.setDesc(items[which]);
        scv_changebg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("归属地提示框风格");
                int item = sp.getInt("which", 0);
                builder.setSingleChoiceItems(items, item, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 保存背景风格
                        Editor editor = sp.edit();
                        editor.putInt("which", which);
                        editor.commit();
                        scv_changebg.setDesc(items[which]);

                        // 取消对话框
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onResume();
        showAddress = new Intent(this, AddressService.class);
        boolean isServiceRunning = ServiceUtils.isServiceRunning(SettingActivity.this,
                "com.itheima.mobilesafe.service.AddressService");
        if (isServiceRunning) {
            siv_show_address.setChecked(true);
        } else {
            siv_show_address.setChecked(false);
        }
    }

}
