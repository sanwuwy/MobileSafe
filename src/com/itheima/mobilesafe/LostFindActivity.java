package com.itheima.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LostFindActivity extends Activity {

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        // 判断一下，是否做过设置向导，如果没有做过，就跳转到设置向导页面去设置，否则就留着当前的页面
        boolean configed = sp.getBoolean("configed", false);
        if (configed) {
            // 就在手机防盗页面
            setContentView(R.layout.activity_lost_find);
            ImageView iv_lock_state = (ImageView) findViewById(R.id.iv_lock_state);
            TextView tv_lock_context = (TextView) findViewById(R.id.tv_lock_context);
            TextView tv_safeNumber = (TextView) findViewById(R.id.tv_safeNumber);
            tv_safeNumber.setText(sp.getString("safeNumber", ""));
            boolean protecting = sp.getBoolean("protecting", false);
            if (protecting) {
                iv_lock_state.setImageResource(R.drawable.lock);
                tv_lock_context.setText("防盗保护已经开启");
            } else {
                iv_lock_state.setImageResource(R.drawable.unlock);
                tv_lock_context.setText("防盗保护还没开启");
            }
        } else {
            // 还没有做过设置向导
            Intent intent = new Intent(this, Setup1Activity.class);
            startActivity(intent);
            // 关闭当前页面
            finish();
        }
    }

    /**
     * 重新进入手机防盗设置向导页面
     *
     * @param view
     */
    public void reEnterSetup(View view) {
        Intent intent = new Intent(this, Setup1Activity.class);
        startActivity(intent);
        // 关闭当前页面
        finish();
    }

}
