package com.genialsir.tvh.utils;

import com.orhanobut.logger.Logger;

import de.robv.android.xposed.XposedBridge;

/**
 * @author genialsir@163.com (GenialSir) on 2019/5/31
 */
public class LoggerUtils {

    private final static String LEFT = "[Genial ---> ";
    private final static String RIGHT = " <--- Sir]";

    //Add log marks.
    private static String flag(String content) {
        return LEFT + content + RIGHT;
    }

    public static void v(String log) {
        Logger.v(flag(log));
    }

    public static void xv(String log) {
        XposedBridge.log(flag(log));
        Logger.v(flag(log));
    }

    public static void d(String log) {
        Logger.d(flag(log));
    }

    public static void xd(String log) {
        XposedBridge.log(flag(log));
        Logger.d(flag(log));
    }

    public static void i(String log) {
        Logger.i(flag(log));
    }

    public static void xi(String log) {
        XposedBridge.log(flag(log));
        Logger.i(flag(log));
    }

    public static void w(String log) {
        Logger.w(flag(log));
    }

    public static void xw(String log) {
        XposedBridge.log(flag(log));
        Logger.w(flag(log));
    }

    public static void e(String log) {
        Logger.e(flag(log));
    }

    public static void xe(String log) {
        XposedBridge.log(flag(log));
        Logger.e(flag(log));
    }

}
