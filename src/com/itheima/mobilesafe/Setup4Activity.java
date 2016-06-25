package com.itheima.mobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Setup4Activity extends BaseSetupActivity {

    private CheckBox cb_proteting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);
        cb_proteting = (CheckBox) findViewById(R.id.cb_proteting);
        boolean protecting = sp.getBoolean("protecting", false);
        if (protecting) {
            cb_proteting.setChecked(true);
            cb_proteting.setText("您已开启防盗保护");
        } else {
            cb_proteting.setChecked(false);
            cb_proteting.setText("您还没有开启防盗保护");
        }
        cb_proteting.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cb_proteting.setText("您已开启防盗保护");
                } else {
                    cb_proteting.setText("您还没有开启防盗保护");
                }
                // 保存选择的状态
                sp.edit().putBoolean("protecting", isChecked).commit();
            }
        });
    }

    @Override
    public void showNext() {
        Editor editor = sp.edit();
        editor.putBoolean("configed", true);
        editor.commit();

        Intent intent = new Intent(this, LostFindActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);
    }

    @Override
    protected void showPre() {
        Intent intent = new Intent(this, Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }
}
