package com.itheima.mobilesafe;

import java.util.List;

import com.itheima.mobilesafe.domain.AppInfo;
import com.itheima.mobilesafe.engine.QueryBootupApplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CleanStartupActivity extends Activity {
    private ListView lv_bootmanger;
    private LinearLayout ll_loading;

    private List<AppInfo> appinfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clean_startup);
        lv_bootmanger = (ListView) findViewById(R.id.lv_bootmanger);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

        lv_bootmanger.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    AppInfo appInfo = appinfos.get(position);
//                    RootTools.sendShell("pm disable " + appInfo.getPackname() + "/" + appInfo.getReceivername(), 3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                appinfos = QueryBootupApplication.getBootupApps(getApplicationContext());
                return null;
            }

            @Override
            protected void onPreExecute() {
                ll_loading.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void result) {
                ll_loading.setVisibility(View.INVISIBLE);
                lv_bootmanger.setAdapter(new MyAdapter());
                super.onPostExecute(result);
            }

        }.execute();
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return appinfos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(getApplicationContext());
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(22);
            tv.setText(appinfos.get(position).getName());
            return tv;
        }

    }
}