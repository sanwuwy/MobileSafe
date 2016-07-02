package com.itheima.mobilesafe;

import com.itheima.mobilesafe.utils.MD5Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {

    protected static final String TAG = "HomeActivity";
    private GridView list_home;
    private SharedPreferences sp;
    private Intent intent;

    private static String [] names = {
            "手机防盗","通讯卫士","软件管理",
            "进程管理","流量统计","手机杀毒",
            "缓存清理","高级工具","设置中心"
        };

    private static int[] ids = {
        R.drawable.safe,R.drawable.callmsgsafe,R.drawable.app,
        R.drawable.taskmanager,R.drawable.netmanager,R.drawable.trojan,
        R.drawable.sysoptimize,R.drawable.atools,R.drawable.settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        list_home = (GridView) findViewById(R.id.list_home);
        list_home.setAdapter(new MyAdapter());
        list_home.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                case 0: // 进入手机防盗页面
                    showLostFindDialog();
                    break;
                case 1: // 进入黑名单拦截界面
                    intent = new Intent(HomeActivity.this, CallSmsSafeActivity.class);
                    startActivity(intent);
                    break;
                case 7:// 进入高级工具
                    intent = new Intent(HomeActivity.this, AtoolsActivity.class);
                    startActivity(intent);
                    break;
                case 8: // 进入设置中心
                    intent = new Intent(HomeActivity.this, SettingActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        });
    }

    private void showLostFindDialog() {
        // 判断是否设置过密码
        if (isSetupPwd()) {
            // 已经设置密码了，弹出的是输入密码对话框
            showEnterDialog();
        } else {
            // 没有设置过密码，弹出的是设置密码对话框
            showSetupPwdDialog();
        }

    }

    private EditText et_setup_pwd, et_setup_confirm, et_enter_pwd;
    private Button ok, cancel;
    private AlertDialog dialog;

    /**
     * 设置密码对话框
     */
    private void showSetupPwdDialog() {
        AlertDialog.Builder builder = new Builder(HomeActivity.this);
        // 自定义一个布局文件
        View view = View.inflate(HomeActivity.this, R.layout.dialog_setup_password, null);
        et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
        et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 把这个对话框取消掉
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 取出密码
                String password = et_setup_pwd.getText().toString().trim();
                String password_confirm = et_setup_confirm.getText().toString().trim();
                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm)) {
                    Toast.makeText(HomeActivity.this, "密码不能为空", 0).show();
                    return;
                }
                // 判断是否一致才去保存
                if (password.equals(password_confirm)) {
                    // 一致的话，就保存密码，把对话框消掉，还要进入手机防盗页面
                    Editor editor = sp.edit();
                    editor.putString("password", MD5Utils.md5Password(password));// 保存加密后的密码
                    editor.commit();
                    dialog.dismiss();
                    Log.i(TAG, "进入手机防盗页面");
                    Intent intent = new Intent(HomeActivity.this, LostFindActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "两次密码不一致，请重新输入", 0).show();
                    et_setup_pwd.setText("");
                    et_setup_pwd.requestFocus();  // 获取焦点
                    et_setup_confirm.setText("");
                    return;
                }
            }
        });
        // builder.setView(view);
        // dialog = builder.show();
        dialog = builder.create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 输入密码对话框
     */
    private void showEnterDialog() {
        AlertDialog.Builder builder = new Builder(HomeActivity.this);
        // 自定义一个布局文件
        View view = View.inflate(HomeActivity.this, R.layout.dialog_enter_password, null);
        et_enter_pwd = (EditText) view.findViewById(R.id.et_enter_pwd);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 把这个对话框取消掉
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 取出密码
                String password = et_enter_pwd.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(HomeActivity.this, "密码为空", 0).show();
                    return;
                }

                String savePassword = sp.getString("password", "");// 取出加密后的密码
                if (MD5Utils.md5Password(password).equals(savePassword)) {
                    // 输入的密码是我之前设置的密码
                    // 把对话框消掉，进入主页面；
                    dialog.dismiss();
                    Log.i(TAG, "把对话框消掉，进入手机防盗页面");
                    Intent intent = new Intent(HomeActivity.this, LostFindActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "密码错误", 0).show();
                    et_enter_pwd.setText("");
                    return;
                }

            }
        });
        dialog = builder.create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 判断是否设置过密码
     * @return
     */
    private boolean isSetupPwd(){
        String password = sp.getString("password", null);
//      if(TextUtils.isEmpty(password)){
//          return false;
//      }else{
//          return true;
//      }
        return !TextUtils.isEmpty(password);

    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return names.length;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(HomeActivity.this, R.layout.list_item_home, null);
            ImageView iv_item = (ImageView) view.findViewById(R.id.iv_item);
            TextView tv_item = (TextView) view.findViewById(R.id.tv_item);

            tv_item.setText(names[position]);
            iv_item.setImageResource(ids[position]);
            return view;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
