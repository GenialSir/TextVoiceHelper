package com.genialsir.tvh;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.genialsir.sgd.config.WE_CHAT_FLAG;
import com.genialsir.tvh.config.APP_PACKAGE_NAME;
import com.genialsir.tvh.db.WeChatDBHelper;
import com.genialsir.tvh.utils.LoggerUtils;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * @author genialsir@163.com (GenialSir) on 2019/5/30
 */
public class CallingTheDog implements IXposedHookLoadPackage {

    //Specify the currently required version.
    public static String currentVersion = WE_CHAT_FLAG.VERSION_6_5_4;
    private WeChatLauncherUI weChatLauncherUI;
    private WeChatDBHelper weChatDBHelper;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam LPParam) throws Throwable {
//        if (APP_PACKAGE_NAME.VOICE_DEMO.equals(LPParam.packageName)) {
//            XposedBridge.log(LoggerUtils.ADD_LOG_FLA("Voice Demo init."));
//            Logger.d(LoggerUtils.ADD_LOG_FLA("Voice Demo init."));
//            voiceDemo(LPParam);
//        }

        if (APP_PACKAGE_NAME.WE_CHAT.equals(LPParam.packageName)) {
            if (weChatLauncherUI == null) {
                LoggerUtils.xd("We Chat init.");
                weChatLauncherUI = new WeChatLauncherUI(LPParam);
            }

            toHookWeChatAttach(LPParam);
        }
    }


    private void toHookWeChatAttach(final XC_LoadPackage.LoadPackageParam lpParam) {
        findAndHookMethod(Application.class, "attach", Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (weChatDBHelper == null) {
                            LoggerUtils.xd("WeChatDBHelper init.");
                            weChatDBHelper = new WeChatDBHelper();
                            weChatDBHelper.init(lpParam);
                        }
                    }
                });
    }

    //测试语音工具类
    private void voiceDemo(XC_LoadPackage.LoadPackageParam LPParam) {
        findAndHookMethod("com.genialsir.voicedemo.MainActivity",
                LPParam.classLoader, "toPlay", View.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object mainObj = param.thisObject;
                        XposedHelpers.setObjectField(mainObj, "content", "这里是管理导航犬发出的哈哈");
//                        EditText etContent = (EditText) XposedHelpers.getObjectField(mainObj, "etContent");
//                        etContent.setText("这里是管理导航犬发出的嗯嗯");
                    }
                }
        );
    }
}
