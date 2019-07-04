package com.genialsir.tvh.utils;

import android.content.Context;

import com.genialsir.tvh.CallingTheDog;
import com.genialsir.sgd.config.WE_CHAT_FLAG;

import de.robv.android.xposed.XposedHelpers;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/10
 */
public class WeChatHelperUtils {

    //Get global context.
    public static Context getContext(ClassLoader classLoader) {
        switch (CallingTheDog.currentVersion) {
            case WE_CHAT_FLAG.VERSION_6_5_4:
                return (Context) XposedHelpers.callStaticMethod(
                        XposedHelpers.findClass("com.tencent.mm.sdk.platformtools.aa",
                                classLoader), "getContext");
            case WE_CHAT_FLAG.VERSION_7_0_4:
                return null;
            default:
                return null;
        }
    }
}
