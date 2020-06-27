package com.taotaoshop.shop;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.xuexiang.xui.XUI;

public class TaoApp extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        XUI.init(this); //初始化UI框架
        XUI.debug(true);  //开启UI框架调试日志
        mContext = getApplicationContext();

    }
}
