package com.itheima.mobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setup3Activity extends BaseSetupActivity {

    private EditText et_safeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup3);
        et_safeNumber = (EditText) findViewById(R.id.et_safeNumber);
        et_safeNumber.setText(sp.getString("safeNumber", ""));
    }

    @Override
    public void showNext() {
        String safeNumber = et_safeNumber.getText().toString().trim();
        if (TextUtils.isEmpty(safeNumber)) {
            Toast.makeText(this, "请输入或设置安全号码", 0).show();
            return;
        }

        // 将安全号码保存起来
        Editor editor = sp.edit();
        editor.putString("safeNumber", safeNumber);
        editor.commit();

        Intent intent = new Intent(this, Setup4Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);
    }

    @Override
    protected void showPre() {
        Intent intent = new Intent(this, Setup2Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }

    /**
     * 选择联系人的点击事件
     * @param view
     */
    public void selectContact(View view) {
        Intent intent = new Intent(this, SelectContactActivity.class);
        // 开启选择联系人界面
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            String phone = data.getStringExtra(getPackageName() + ".Phone").replace("-", "").replace(" ", "");
            et_safeNumber.setText(phone);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
