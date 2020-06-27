package com.taotaoshop.shop.utils;

import android.content.Context;

import com.taotaoshop.shop.R;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.toast.XToast;

public class AppInfo {

    public static void Tsuccess(Context context, String message){
        XToast.success(context, message, XToast.LENGTH_LONG).show();
    }
    public static void Terror(Context context, String message){
        XToast.error(context, message).show();
    }
    public static void Twarning(Context context, String message){
        XToast.warning(context, message).show();
    }
    public static void Tinfo(Context context, String message){
        XToast.info(context, message).show();
    }
    public static void Tnormal(Context context, String message){
        XToast.normal(context, message).show();
    }

    public static void DInfo(Context context, String title, String message){
        new MaterialDialog.Builder(context)
                .iconRes(R.mipmap.icon_tip)
                .title(title)
                .content(message)
                .positiveText("确定")
                .show();
    }

    public static void Derror(Context context, String title, String message){
        new MaterialDialog.Builder(context)
                .iconRes(R.mipmap.icon_warning)
                .title(title)
                .content(message)
                .positiveText("确定")
                .show();
    }

    public static Boolean t(){
        return true;
    }
    public static Boolean f(){
        return false;
    }
}
