package com.genialsir.tvh.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.genialsir.tvh.utils.LoggerUtils;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/26
 */
public class KeyMonitorService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LoggerUtils.d("onServiceConnected");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LoggerUtils.d("onUnbind");
        return super.onUnbind(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkgName = event.getPackageName().toString();
        String className = event.getClassName().toString();
        int eventType = event.getEventType();
        int contentChangeType = event.getContentChangeTypes();
//        int action = event.getAction();
//        LoggerUtils.d("pkgName is " + pkgName);
//        LoggerUtils.d("className is " + className);
//        LoggerUtils.d("eventType is " + eventType);
        LoggerUtils.d("contentChangeType is " + contentChangeType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }


    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        LoggerUtils.d("key = " + key);
        switch (key) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                LoggerUtils.d("KEYCODE_VOLUME_UP");
                break;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                LoggerUtils.d("KEYCODE_VOLUME_DOWN");
                break;
        }
        return super.onKeyEvent(event);
    }


}
