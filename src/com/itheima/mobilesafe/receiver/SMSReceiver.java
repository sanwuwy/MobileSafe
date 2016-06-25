package com.itheima.mobilesafe.receiver;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.service.GPSService;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";

    private SharedPreferences sp;
    // 设备策略管理器
    private DevicePolicyManager dpm;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus != null) {
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String sender = smsMessage.getOriginatingAddress();
                String body = smsMessage.getMessageBody();
                Log.i(TAG, "sender = " + sender + ", body = " + body);
                sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                String safeNumber = sp.getString("safeNumber", "");
                dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (sender.equals(safeNumber)) {
                    if ("#*location*#".equals(body)) {
                        // 得到手机的GPS
                        Log.i(TAG, "得到手机的GPS");
                        // 启动GPSService服务
                        Intent i = new Intent(context, GPSService.class);
                        context.startService(i);
                        String lastlocation = sp.getString("lastlocation", null);
                        if (TextUtils.isEmpty(lastlocation)) {
                            // 位置没有得到
                            SmsManager.getDefault().sendTextMessage(sender, null, "Getting loaction.....", null, null);
                        } else {
                            SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);
                        }

                        // 把这个广播终止掉
                        abortBroadcast();
                    } else if ("#*alarm*#".equals(body)) {
                        // 播放报警影音
                        Log.i(TAG, "播放报警影音");
                        MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
                        player.setLooping(false);//
                        player.setVolume(1.0f, 1.0f);
                        player.start();

                        // 振动
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        long[] pattern = { 200, 300, 400, 500 };
                        vibrator.vibrate(pattern, 0);// 循环振动

                        abortBroadcast();
                    } else if ("#*wipedata*#".equals(body)) {
                        // 远程清除数据
                        Log.i(TAG, "远程清除数据");
                        ComponentName who_wipedata = new ComponentName(context, LockScreenReceiver.class);
                        if (dpm.isAdminActive(who_wipedata)) {
                            // 清除手机SD卡上的数据
                            dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                            // 恢复出厂设置
                            dpm.wipeData(0);
                        }
                        abortBroadcast();
                    } else if ("#*lockscreen*#".equals(body)) {
                        // 远程锁屏
                        Log.i(TAG, "远程锁屏");
                        ComponentName who_wipedata = new ComponentName(context, LockScreenReceiver.class);
                        if (dpm.isAdminActive(who_wipedata)) {
                            // dpm.resetPassword("123456", 0); // 设置屏蔽密码
                            dpm.lockNow();
                        }
                        abortBroadcast();
                    }
                }
            }
        }
    }

}
