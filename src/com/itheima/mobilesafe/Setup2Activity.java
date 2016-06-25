package com.itheima.mobilesafe;

import com.itheima.mobilesafe.ui.SettingItemView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class Setup2Activity extends BaseSetupActivity {

    private SettingItemView siv_bindSIM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
        siv_bindSIM = (SettingItemView) findViewById(R.id.siv_bindSIM);
        String bindSIM = sp.getString("bindSIM", null);
        if (!TextUtils.isEmpty(bindSIM)) {
            // SIM卡已经绑定
            siv_bindSIM.setChecked(true);
        } else {
            // SIM卡还未绑定
            siv_bindSIM.setChecked(false);
        }
        siv_bindSIM.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Editor editor = sp.edit();
                // 获取当前绑定状态
                if (siv_bindSIM.isChecked()) { // 已经绑定了
                    siv_bindSIM.setChecked(false);
                    editor.putString("bindSIM", null);
                } else { // 还未绑定
                    siv_bindSIM.setChecked(true);
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    editor.putString("bindSIM", tm.getSimSerialNumber()); // 保存sim卡的序列号
                }
                editor.commit();
            }
        });
    }

    @Override
    public void showNext() {
        String bindSIM = sp.getString("bindSIM", null);
        if (TextUtils.isEmpty(bindSIM)) {
            Toast.makeText(this, "还没有绑定SIM卡，请绑定", 0).show();
            return;
        }
        Intent intent = new Intent(this, Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);
    }

    @Override
    protected void showPre() {
        Intent intent = new Intent(this, Setup1Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }

}
