package com.itheima.mobilesafe;

import com.itheima.mobilesafe.utils.SmsUtils;
import com.itheima.mobilesafe.utils.SmsUtils.BackUpCallBack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AtoolsActivity extends Activity {

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atools);
    }

    /**
     * 点击事件，进入号码归属地查询的页面
     *
     * @param view
     */
    public void numberQuery(View view) {
        Intent intent = new Intent(this, NumberAddressQueryActivity.class);
        startActivity(intent);

    }

    /**
     * 点击事件，短信的备份
     *
     * @param view
     */
    public void smsBackup(View view) {
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在备份短信");
        pd.show();
        // 短信的备份是一个耗时的操作，应该放在子线程中运行
        new Thread() {
            @Override
            public void run() {
                try {
                    SmsUtils.backupSms(AtoolsActivity.this, new BackUpCallBack() {
                        @Override
                        public void beforeBackup(int max) {
                            pd.setMax(max);
                        }

                        @Override
                        public void onSmsBackup(int progress) {
                            pd.setProgress(progress);
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AtoolsActivity.this, "备份成功", 0).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AtoolsActivity.this, "备份失败", 0).show();
                        }
                    });
                } finally {
                    pd.dismiss();
                }
            };
        }.start();

    }

    /**
     * 点击事件，短信的还原
     *
     * @param view
     */
    public void smsRestore(View view) {

        SmsUtils.restoreSms(this, true);
        Toast.makeText(this, "还原成功", 0).show();
    }

}
