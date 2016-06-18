package com.itheima.mobilesafe;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.itheima.mobilesafe.utils.StreamTools;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

    private static final int ENTER_HOME = 0;
    private static final int SHOW_UPDATE_DIALOG = 1;
    private static final int URL_ERROR = 2;
    private static final int NETWORK_ERROR = 3;
    private static final int JSON_ERROR = 4;
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("版本号：" + getVersion());

        checkUpdate();
        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(2000);
        RelativeLayout rl_root_splash = (RelativeLayout) findViewById(R.id.rl_root_splash);
        rl_root_splash.setAnimation(aa);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ENTER_HOME: // 进入主页面
                enterHome();
                break;
            case SHOW_UPDATE_DIALOG: // 显示升级的对话框
                Log.i(TAG, "显示升级的对话框");
                break;
            case URL_ERROR: // URL错误
                enterHome();
                Toast.makeText(SplashActivity.this, "URL错误", 0).show();
                break;
            case NETWORK_ERROR: // 网络连接异常
                enterHome();
                Toast.makeText(SplashActivity.this, "网络连接异常", 0).show();
                break;
            case JSON_ERROR: // JSON解析出错
                enterHome();
                Toast.makeText(SplashActivity.this, "JSON解析出错", 0).show();
                break;
            }
        }

    };

    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 检查是否有新版本，如果有就升级
     */
    private void checkUpdate() {
        new Thread() {
            @Override
            public void run() {
                long startTime = SystemClock.elapsedRealtime();
                Message msg = Message.obtain();
                try {
                    URL url = new URL(getString(R.string.serverurl));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == 200) { // 联网成功
                        InputStream is = conn.getInputStream();
                        // 把服务返回的输入流转换成字符串
                        String result = StreamTools.readInputStream(is);

                        // json解析
                        JSONObject json = new JSONObject(result);
                        // 得到服务器端APK的版本信息
                        String version = (String) json.get("version");
                        String description = (String) json.get("description");
                        String apkurl = (String) json.get("apkurl");

                        if (getVersion().equals(version)) {
                            // 版本一致，没有新版本，进入主页面
                            msg.what = ENTER_HOME;
                        } else {
                            // 有新版本，弹出一升级对话框
                            msg.what = SHOW_UPDATE_DIALOG;
                        }
                    }
                } catch (MalformedURLException e) {
                    msg.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    msg.what = NETWORK_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    msg.what = JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    long endTime = SystemClock.elapsedRealtime();
                    long spendTime = endTime - startTime;
                    try {
                        Thread.sleep(2000 - spendTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendMessage(msg);
                }
            }
        }.start();
    };

    /**
     * 得到应用程序的版本名称
     *
     * @return
     */
    private String getVersion() {
        // 用来管理手机中的APK
        PackageManager pm = getPackageManager();
        try {
            // 得到指定apk的清单文件
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
