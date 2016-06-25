package com.itheima.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

public class BootCompleteReceiver extends BroadcastReceiver {

    private SharedPreferences sp;
    private TelephonyManager tm;

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean protecting = sp.getBoolean("protecting", false);
        if (protecting) {
            // 读取之前保存的SIM信息；
            String bindSIM = sp.getString("bindSIM", "");

            tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // 读取当前的SIM卡信息
            String realSim = tm.getSimSerialNumber();

            if (!bindSIM.equals(realSim)) { // SIM卡已经变更 发一个短信给安全号码
                System.out.println("SIM卡 已经变更");
                SmsManager.getDefault().sendTextMessage(sp.getString("safeNumber", ""), null, "SIM card changed", null, null);
            }
        }
    }

}
