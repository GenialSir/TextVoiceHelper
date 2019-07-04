package com.genialsir.tvh;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.genialsir.tvh.service.VoiceSocketManager;
import com.genialsir.tvh.utils.LoggerUtils;
import com.genialsir.tvh.utils.ToastUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_NICKNAME = "TVH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLog();
        initSocket();
    }

    private void initSocket() {
        //启动Socket服务类
        Intent serviceIntent = new Intent(MainActivity.this, VoiceSocketManager.class);
        startService(serviceIntent);
    }

    private void initLog() {
        if (BuildConfig.DEBUG) {
            PrettyFormatStrategy prettyFormatStrategy = PrettyFormatStrategy.newBuilder()
                    .tag(LOG_NICKNAME)
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter(prettyFormatStrategy));
        } else {
            CsvFormatStrategy csvFormatStrategy = CsvFormatStrategy.newBuilder()
                    .tag(LOG_NICKNAME)
                    .build();
            Logger.clearLogAdapters();
            Logger.addLogAdapter(new DiskLogAdapter(csvFormatStrategy));
        }
    }

    public void onSpeak(View view) {
        ToastUtils.showToast(MainActivity.this, "TextVoiceHelper");
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

}