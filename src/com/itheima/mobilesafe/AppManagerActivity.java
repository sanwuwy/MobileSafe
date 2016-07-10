package com.itheima.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import com.itheima.mobilesafe.domain.AppInfo;
import com.itheima.mobilesafe.engine.AppInfoProvider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AppManagerActivity extends Activity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
        tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
        // /storage/sdcard 对应USB存储空间
        long sdsize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
        // /data 对应手机内部存储空间
        long romsize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());
        tv_avail_sd.setText("SD卡可用空间：" + Formatter.formatFileSize(this, sdsize));
        tv_avail_rom.setText("内存可用空间：" + Formatter.formatFileSize(this, romsize));

        lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        fillData();
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
                tv.setText("用户程序：" + userAppInfos.size() + "个");
                return tv;
            } else if (position == (userAppInfos.size() + 1)) {
                TextView tv = new TextView(getApplicationContext());
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextSize(16.0f);
                tv.setText("系统程序：" + systemAppInfos.size() + "个");
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
}
