package com.itheima.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import com.itheima.mobilesafe.domain.AppInfo;
import com.itheima.mobilesafe.engine.AppInfoProvider;
import com.itheima.mobilesafe.utils.DensityUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AppManagerActivity extends Activity implements OnClickListener {
    private static final String TAG = "AppManagerActivity";
    private TextView tv_avail_rom;
    private TextView tv_avail_sd;

    private ListView lv_app_manager;
    private LinearLayout ll_loading;

    /**
     * 所有的应用程序包信息
     */
    private List<AppInfo> appInfos;

    /**
     * 用户应用程序的集合
     */
    private List<AppInfo> userAppInfos;

    /**
     * 系统应用程序的集合
     */
    private List<AppInfo> systemAppInfos;

    private AppManagerAdapter adapter;

    /**
     * 当前程序信息的状态。
     */
    private TextView tv_status;

    /**
     * 弹出悬浮窗体
     */
    private PopupWindow popupWindow;

    /**
     * 开启
     */
    private LinearLayout ll_start;
    /**
     * 分享
     */
    private LinearLayout ll_share;
    /**
     * 卸载
     */
    private LinearLayout ll_uninstall;

    /**
     * 被点击的条目。
     */
    private AppInfo appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
        tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
        tv_status = (TextView) findViewById(R.id.tv_status);
        // /storage/sdcard 对应USB存储空间
        long sdsize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
        // /data 对应手机内部存储空间
        long romsize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());
        tv_avail_sd.setText("SD卡可用空间：" + Formatter.formatFileSize(this, sdsize));
        tv_avail_rom.setText("内存可用空间：" + Formatter.formatFileSize(this, romsize));

        lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        fillData();
        // 给listview注册一个滚动的监听器
        lv_app_manager.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            /**
             * 当Listview滚动时调用
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userAppInfos != null && systemAppInfos != null) {
                    // 将tv_status显示出来
                    tv_status.setVisibility(View.VISIBLE);
                    if (firstVisibleItem > userAppInfos.size()) {
                        tv_status.setText(" 系统程序：" + systemAppInfos.size() + "个");
                    } else {
                        tv_status.setText(" 用户程序：" + userAppInfos.size() + "个");
                    }
                }
            }
        });

        /**
         * 设置listview的点击事件
         */
        lv_app_manager.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                } else if (position == (userAppInfos.size() + 1)) {
                    return;
                } else if (position <= userAppInfos.size()) {// 用户程序
                    int newposition = position - 1;
                    appInfo = userAppInfos.get(newposition);
                } else {// 系统程序
                    int newposition = position - 1 - userAppInfos.size() - 1;
                    appInfo = systemAppInfos.get(newposition);
                }
                // System.out.println(appInfo.getPackname());
                dismissPopupWindow();
                View contentView = View.inflate(getApplicationContext(), R.layout.popup_app_item, null);
                ll_start = (LinearLayout) contentView.findViewById(R.id.ll_start);
                ll_share = (LinearLayout) contentView.findViewById(R.id.ll_share);
                ll_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_uninstall);

                ll_start.setOnClickListener(AppManagerActivity.this);
                ll_share.setOnClickListener(AppManagerActivity.this);
                ll_uninstall.setOnClickListener(AppManagerActivity.this);

                popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                // 动画效果的播放必须要求窗体有背景颜色。透明颜色也是颜色
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int[] location = new int[2];
                view.getLocationInWindow(location);
                // 在代码里面设置的宽高值 都是像素。---》dip
                int dip = 60;
                int px = DensityUtil.dip2px(getApplicationContext(), dip);
                System.out.println("px=" + px);
                popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP, px, location[1]);
                ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(300);
                AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
                aa.setDuration(300);
                AnimationSet set = new AnimationSet(false);
                set.addAnimation(aa);
                set.addAnimation(sa);
                contentView.startAnimation(set);
            }
        });

    }

    protected void dismissPopupWindow() {
        // 把旧的弹出窗体关闭掉。
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private void fillData() {
        ll_loading.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
                userAppInfos = new ArrayList<AppInfo>();
                systemAppInfos = new ArrayList<AppInfo>();
                for (AppInfo info : appInfos) {
                    if (info.isUserApp()) {
                        userAppInfos.add(info);
                    } else {
                        systemAppInfos.add(info);
                    }
                }
                // 加载listview的数据适配器
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) {
                            adapter = new AppManagerAdapter();
                            lv_app_manager.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        ll_loading.setVisibility(View.INVISIBLE);
                    }
                });
            };
        }.start();
    }

    private class AppManagerAdapter extends BaseAdapter {

        // 控制listview有多少个条目
        @Override
        public int getCount() {
            // return appInfos.size();
            return userAppInfos.size() + 1 + systemAppInfos.size() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppInfo appInfo;
            if (position == 0) {// 显示的是用程序有多少个的小标签
                TextView tv = new TextView(getApplicationContext());
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextSize(16.0f);
                tv.setText(" 用户程序：" + userAppInfos.size() + "个");
                return tv;
            } else if (position == (userAppInfos.size() + 1)) {
                TextView tv = new TextView(getApplicationContext());
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextSize(16.0f);
                tv.setText(" 系统程序：" + systemAppInfos.size() + "个");
                return tv;
            } else if (position <= userAppInfos.size()) {// 用户程序
                int newposition = position - 1;// 因为多了一个textview的文本占用了位置
                appInfo = userAppInfos.get(newposition);
            } else {// 系统程序
                int newposition = position - 1 - userAppInfos.size() - 1;
                appInfo = systemAppInfos.get(newposition);
            }
            View view;
            ViewHolder holder;

            // if (position < userAppInfos.size()) {// 这些位置是留个用户程序显示的。
            // appInfo = userAppInfos.get(position);
            // } else {// 这些位置是留个系统程序的。
            // int newposition = position - userAppInfos.size();
            // appInfo = systemAppInfos.get(newposition);
            // }
            if (convertView != null && convertView instanceof RelativeLayout) {
                // 不仅需要检查是否为空，还要判断是否是合适的类型去复用
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(getApplicationContext(), R.layout.list_item_appinfo, null);
                holder = new ViewHolder();
                holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
                holder.tv_location = (TextView) view.findViewById(R.id.tv_app_location);
                holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
                view.setTag(holder);
            }
            holder.iv_icon.setImageDrawable(appInfo.getIcon());
            holder.tv_name.setText(appInfo.getName());
            if (appInfo.isInRom()) {
                holder.tv_location.setText("手机内存");
            } else {
                holder.tv_location.setText("外部存储");
            }
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

    private static class ViewHolder {
        TextView tv_name;
        TextView tv_location;
        ImageView iv_icon;
    }

    /**
     * 获取某个目录的可用空间
     *
     * @param path
     * @return
     */
    @SuppressWarnings("deprecation")
    private long getAvailSpace(String path) {
        StatFs statf = new StatFs(path);
        statf.getBlockCount();// 获取所有区块的个数
        long size = statf.getBlockSize();// 获取每个区块的大小
        long count = statf.getAvailableBlocks();// 获取可用的区块的个数
        return size * count;
    }

    @Override
    protected void onDestroy() {
        dismissPopupWindow();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        dismissPopupWindow();
        switch (v.getId()) {
        case R.id.ll_share:
            Log.i(TAG, "分享：" + appInfo.getName());
            shareApplication(appInfo.getName());
            break;
        case R.id.ll_start:
            Log.i(TAG, "启动：" + appInfo.getName());
            startApplication(appInfo.getPackname());
            break;
        case R.id.ll_uninstall:
            if (appInfo.isUserApp()) {
                Log.i(TAG, "卸载：" + appInfo.getName());
                uninstallAppliation(appInfo.getPackname());
            } else {
                Toast.makeText(this, "系统应用只有获取root权限才可以卸载", 0).show();
                // Runtime.getRuntime().exec("");
            }
            break;
        }
    }

    /**
     * 分享一个应用程序
     */
    private void shareApplication(String appName) {
        // Intent { act=android.intent.action.SEND typ=text/plain flg=0x3000000
        // cmp=com.android.mms/.ui.ComposeMessageActivity (has extras) } from
        // pid 256
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "推荐您使用一款软件，名称叫：" + appName);
        startActivity(intent);
    }

    /**
     * 卸载应用
     */
    private void uninstallAppliation(String packageName) {
        // <action android:name="android.intent.action.VIEW" />
        // <action android:name="android.intent.action.DELETE" />
        // <category android:name="android.intent.category.DEFAULT" />
        // <data android:scheme="package" />
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + packageName));
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 刷新界面。
        fillData();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 开启一个应用程序
     */
    private void startApplication(String packageName) {
        // 查询这个应用程序的入口activity。 把他开启起来。
        PackageManager pm = getPackageManager();
        // Intent intent = new Intent();
        // intent.setAction("android.intent.action.MAIN");
        // intent.addCategory("android.intent.category.LAUNCHER");
        // // 查询出来了所有的手机上具有启动能力的activity。
        // List<ResolveInfo> infos = pm.queryIntentActivities(intent,
        // PackageManager.GET_INTENT_FILTERS);
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "不能启动当前应用", 0).show();
        }
    }
}
