package com.genialsir.tvh.db;

import android.database.Cursor;

import com.genialsir.tvh.CallingTheDog;
import com.genialsir.sgd.config.WE_CHAT_FLAG;
import com.genialsir.tvh.msg.MsgListeners;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/6
 */
public class WeChatDBHelper {

    public static Method method = null;
    public static Object receiver = null;

    /**
     * 微信Cursor读写初始化。
     */
    public void init(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        String targetSqlClass = "";
        if(WE_CHAT_FLAG.VERSION_6_5_4.equals(CallingTheDog.currentVersion)){
            targetSqlClass = "com.tencent.mm.bg.g";
        }else if(WE_CHAT_FLAG.VERSION_7_0_4.equals(CallingTheDog.currentVersion)){
            targetSqlClass = "com.tencent.mm.bb.g";
        }
        String targetSqlMethod = "rawQuery";
        findAndHookMethod(targetSqlClass, loadPackageParam.classLoader, targetSqlMethod,
                String.class, String[].class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //hook数据库连接对象，用于发起数据主动查询
                        if(method == null){
                            method = (Method) param.method;
                            receiver = param.thisObject;
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        //监听所有的消息查询语句
                        Cursor result = (Cursor) param.getResult();
                        String sqlStr = String.valueOf(param.args[0]);
                        if (result != null && result.getCount()>0 && sqlStr.startsWith("select * from message")) {
                            MsgListeners.listenerPath(result, loadPackageParam);
                        }
                    }
                });
    }
}
