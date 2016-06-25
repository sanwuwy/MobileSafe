package com.itheima.mobilesafe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SelectContactActivity extends Activity {

    private ListView lv_select_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        lv_select_contact = (ListView) findViewById(R.id.lv_select_contact);
        final List<Map<String, String>> data = getContacts();
        lv_select_contact.setAdapter(new SimpleAdapter(this, data, R.layout.contact_item_view,
                new String[] { "name", "phone" }, new int[] { R.id.tv_name, R.id.tv_phone }));
        Intent result = new Intent();
        lv_select_contact.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phone = data.get(position).get("phone");
                Intent result = new Intent();
                result.putExtra(getPackageName() + ".Phone", phone);
                setResult(RESULT_OK, result);
                // 当前页面关闭掉
                finish();
            }
        });
    }

    /*
     * 查询数据库，获取所有联系人号码
     */
    private List<Map<String, String>> getContacts() {
        HashMap<String, String> contact = null;
        ContentResolver resolver = this.getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        // 先查询出所有联系人的contact_id
        Cursor cursor = resolver.query(uri, null, null, null, null);
        List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext()) {
            String contact_id = cursor.getString(cursor.getColumnIndex("contact_id"));
            if (contact_id != null) {
                contact = new HashMap<String, String>();
                // 根据contact_id查询出该联系人每条详细信息
                Cursor dataCursor = resolver.query(dataUri, new String[] { "data1", "mimetype" }, "raw_contact_id=?",
                        new String[] { contact_id }, null);
                while (dataCursor.moveToNext()) {

                    String data1 = dataCursor.getString(0);
                    String mimetype = dataCursor.getString(1);

                    // 判断当前条目是否为联系人的姓名
                    if ("vnd.android.cursor.item/name".equals(mimetype)) {
                        contact.put("name", "姓名：" + data1);
                    }
                    // 判断当前条目是否为联系人的号码
                    if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                        contact.put("phone", "电话：" + data1);
                    }
                }
                contacts.add(contact);
                dataCursor.close();
            }
        }
        cursor.close();
        return contacts;
    }

}
