package com.itheima.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import com.itheima.mobilesafe.domain.AppTraffic;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TrafficManagerActivity extends Activity {
    ProgressBar pb_traffic_manager;
    ListView lv_traffic_manager;

    List<AppTraffic> appTraffics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_manager);
        lv_traffic_manager = (ListView) findViewById(R.id.lv_traffic_manager);
        pb_traffic_manager = (ProgressBar) findViewById(R.id.pb_traffic_manager);
        fillData();
    }

    private void fillData() {
        new Thread(){
            @Override
            public void run() {
                pb_traffic_manager.setVisibility(View.VISIBLE);
                appTraffics = getTrafficCount();
                pb_traffic_manager.setVisibility(View.INVISIBLE);
                // 加载listview的数据适配器
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv_traffic_manager.setAdapter(new TrafficAdapter());
                    };
                });
            };
        }.start();

    }

    class TrafficAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return appTraffics.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                holder = new ViewHolder();
                view = View.inflate(TrafficManagerActivity.this, R.layout.list_item_trafficinfo, null);
                holder.iv_app_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
                holder.tv_app_name = (TextView) view.findViewById(R.id.tv_app_name);
                holder.tv_app_traffic = (TextView) view.findViewById(R.id.tv_app_traffic);
                view.setTag(holder);
            }
            AppTraffic appTraffic = appTraffics.get(position);
            holder.iv_app_icon.setImageDrawable(appTraffic.getIcon());
            holder.tv_app_name.setText(appTraffic.getName());
            holder.tv_app_traffic.setText(
                    "上传：" + Formatter.formatFileSize(TrafficManagerActivity.this, appTraffic.getUidTxBytes()) +
                    "\t\t下载：" + Formatter.formatFileSize(TrafficManagerActivity.this, appTraffic.getUidRxBytes()));
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
        TextView tv_app_name;
        TextView tv_app_traffic;
        ImageView iv_app_icon;
    }

    private List<AppTraffic> getTrafficCount() {
        PackageManager pm = getPackageManager();
        // 遍历 /data/data/包 应用下面的AndroidManifest.xml文件里面的权限
        List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        List<AppTraffic> appTraffics = new ArrayList<AppTraffic>();

        for (PackageInfo info : infos) {
            String[] permissions = info.requestedPermissions;
            if (permissions != null && permissions.length > 0) {
                for (String p : permissions) {
                    if ("android.permission.INTERNET".equals(p)) {// 如果有网络访问权限
                        AppTraffic appTraffic = new AppTraffic();
                        System.out.println(info.applicationInfo.loadLabel(pm) + "访问网络.");

                        Drawable icon = info.applicationInfo.loadIcon(pm);
                        int uid = info.applicationInfo.uid;
                        String name = info.applicationInfo.loadLabel(pm).toString();
                        appTraffic.setIcon(icon);
                        appTraffic.setUid(uid);// 获取系统分配 应用的uid
                        appTraffic.setName(name);
                        long rx = TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getUidRxBytes(uid);
                        long tx = TrafficStats.getUidTxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getUidTxBytes(uid);
                        appTraffic.setUidRxBytes(rx);
                        appTraffic.setUidTxBytes(tx);
                        System.out.print("name = " + name + ", uid = " + uid + ", rx = " + rx + ", tx = " + tx);

                        appTraffics.add(appTraffic);
                    }
                }
            }
            /*
             * TrafficStats.getUidRxBytes(uid); // 获取指定UID程序的下载数据
             * TrafficStats.getUidTxBytes(uid); // 获取指定UID程序的上传数据.
             *
             * TrafficStats.getMobileRxBytes(); // 2g/3g 下载的总流量
             * TrafficStats.getMobileTxBytes(); //
             *
             * TrafficStats.getTotalRxBytes(); // 2g/3g wifi
             * TrafficStats.getTotalTxBytes();
             * 方法返回-1代码应用程序没有产生流量信息或者操作系统不支持流量统计
             */
        }
        return appTraffics;
    }
}
