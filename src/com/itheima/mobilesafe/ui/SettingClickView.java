package com.itheima.mobilesafe.ui;

import com.itheima.mobilesafe.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我们自定义的组合控件，它里面有两个TextView，还有一个ImageView，还有一个View
 *
 * @author Administrator
 *
 */
public class SettingClickView extends RelativeLayout {

    private TextView tv_desc;
    private TextView tv_title;

    /**
     * 初始化布局文件
     *
     * @param context
     */
    private void initView(Context context) {

        // 把一个布局文件转化为View，并且加载到SettingItemView
        View.inflate(context, R.layout.setting_click_view, this);
        tv_desc = (TextView) this.findViewById(R.id.tv_desc);
        tv_title = (TextView) this.findViewById(R.id.tv_title);

    }

    /**
     * 自己通过new SettingItemView(context) 创建该View对象的时候调用
     *
     * @param context
     */
    public SettingClickView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * 带有两个参数的构造方法，系统在加载布局文件的时候调用
     *
     * @param context
     * @param attrs
     */
    public SettingClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        String title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.itheima.mobilesafe", "title");
        String desc_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.itheima.mobilesafe", "desc_on");
        tv_title.setText(title);
        setDesc(desc_on);
    }

    public SettingClickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    /**
     * 设置组合控件的描述信息
     */
    public void setDesc(String text) {
        tv_desc.setText(text);
    }

}
