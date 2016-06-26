package com.itheima.mobilesafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.itheima.mobilesafe.utils.StreamTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

public class SplashActivity extends Activity {

    private static final int ENTER_HOME = 0;
    private static final int SHOW_UPDATE_DIALOG = 1;
    private static final int URL_ERROR = 2;
    private static final int NETWORK_ERROR = 3;
    private static final int JSON_ERROR = 4;
    protected static final String TAG = "SplashActivity";
    private String description;
    private String apkurl;
    private TextView tv_update_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("版本号：" + getVersion());
        tv_update_progress = (TextView) findViewById(R.id.tv_update_progress);
        //拷贝数据库
        copyDB();
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        boolean update = sp.getBoolean("update", true);
        if (update) {
            // 检查升级
            checkUpdate();
        } else { // 自动升级已经关闭
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // 进入主页面
                    enterHome();
                }
            }, 2000);
        }
        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(2000);
        RelativeLayout rl_root_splash = (RelativeLayout) findViewById(R.id.rl_root_splash);
        rl_root_splash.setAnimation(aa);
    }

    /**
     * path 把address.db这个数据库拷贝到data/data/《包名》/files/address.db
     */
    private void copyDB() {
        try {
            File file = new File(getFilesDir(), "address.db");
            if (file.exists()) {
                Log.i(TAG, "数据库已存在，不需要拷贝了");
            } else {
                InputStream is = getAssets().open("address.db");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[4 * 1024];
                int len = -1;
                while((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                showUpdateDialog();
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

    /**
     * 进入主界面
     */
    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 显示下载对话框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("提示升级");
        builder.setMessage(description);
        // builder.setCancelable(false);//强制升级
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // 进入主页面
                enterHome();
                dialog.dismiss();

            }
        });
        builder.setPositiveButton("立刻升级", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 下载APK，替换升级
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    FinalHttp finalHttp = new FinalHttp();
                    finalHttp.download(apkurl,
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileSafe.apk",
                            new AjaxCallBack<File>() {

                                @Override
                                public void onFailure(Throwable t, int errorNo, String strMsg) {
                                    Toast.makeText(SplashActivity.this, "APK下载失败", 1).show();
                                    t.printStackTrace();
                                    super.onFailure(t, errorNo, strMsg);
                                }

                                @Override
                                public void onLoading(long count, long current) {
                                    super.onLoading(count, current);
                                    byte progress = (byte) (current / count * 100);
                                    tv_update_progress.setVisibility(View.VISIBLE);
                                    tv_update_progress.setText("下载进度：" + progress + "%");
                                }

                                @Override
                                public void onSuccess(File file) {
                                    super.onSuccess(file);
                                    installAPK(file);
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "没有检测到SD卡，请安装上SD卡后重试", 1).show();
                    return;
                }

            }
        });
        builder.setNegativeButton("下次再说", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消对话款，进入主界面
                dialog.dismiss();
                enterHome();
            }
        });
        builder.show();
    }

    /**
     * 安装APK
     *
     * @param file
     */
    private void installAPK(File file) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        Uri uri = Uri.fromFile(file);
        Log.i(TAG, uri.toString()); // uri =
                                    // file:///storage/sdcard/MobileSafe.apk
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        // 如果用户在 install APK 界面选择了 cancel，那么应该进入主界面
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            enterHome();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                        description = (String) json.get("description");
                        apkurl = (String) json.get("apkurl");

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
}
