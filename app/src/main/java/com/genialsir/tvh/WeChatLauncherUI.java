package com.genialsir.tvh;

import android.app.Activity;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * @author genialsir@163.com (GenialSir) on 2019/5/31
 */
public class WeChatLauncherUI {

    private Activity launcherUI;

    public WeChatLauncherUI(final XC_LoadPackage.LoadPackageParam lpParam) {

        findAndHookMethod("com.tencent.mm.ui.LauncherUI", lpParam.classLoader,
                "onCreate", Bundle.class, new XC_MethodHook() {


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        launcherUI = (Activity) param.thisObject;
                    }
                });
    }

}
