package com.genialsir.tvh.utils;

import android.content.Context;
import android.widget.Toast;


/**
 * @author zhang.bolun@onlylady.com (ZBLemon) on 2016/7/22
 */
public class ToastUtils {

    private static Toast toast;

    public static void showToast(Context context, String msg){
        if(toast == null){
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }else{
            //不为空直接改变当前Toast文本
            toast.setText(msg);
        }
        toast.show();
    }

    public static void showShortToast(Context context,  String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
