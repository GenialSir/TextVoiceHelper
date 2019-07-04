package com.genialsir.tvh.provider;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import com.genialsir.tvh.db.QueryWeChatDB;
import com.genialsir.tvh.utils.LoggerUtils;
import com.genialsir.tvh.utils.WeChatHelperUtils;
import com.genialsir.tvh.voice.DogInterpret;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/10
 */
public class VolumeProvider {

    private Context mContext;
    private SettingsContentObserver mSettingsContentObserver;
    private AudioManager mAudioManager;
    private String mCurrentWeChatID;
    private XC_LoadPackage.LoadPackageParam mLoadPackageParam;
    private int currentVolume;


    public VolumeProvider(XC_LoadPackage.LoadPackageParam loadPackageParam){
        mContext = WeChatHelperUtils.getContext(loadPackageParam.classLoader);
        mLoadPackageParam = loadPackageParam;
        registerVolumeChangeReceiver();
    }

    public void setCurrentWeChatID(String currentWeChatID) {
        mCurrentWeChatID = currentWeChatID;
    }

    private void registerVolumeChangeReceiver() {
        mSettingsContentObserver = new SettingsContentObserver(null);
        mContext.getContentResolver().registerContentObserver(android.provider
                .Settings.System.CONTENT_URI, true, mSettingsContentObserver);
        //获取系统的Audio管理者
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public class SettingsContentObserver extends ContentObserver {

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (mAudioManager != null) {


                //最小音量
                int minVolume = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    minVolume = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
                }
                //最大音量
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                //当前音量
                currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumeIndex = (maxVolume - minVolume) / 2;

                //查询卷的流类型。
                //要查询卷的卷索引。索引值必须介于给定流类型的最小和最大索引值之间。
                //查询卷的音频输出设备的类型。
                float streamDecibel = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    streamDecibel = mAudioManager.getStreamVolumeDb(AudioManager.STREAM_MUSIC,
                            volumeIndex, AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
                }
                LoggerUtils.xd("minVolume:" + minVolume);
                LoggerUtils.xd("maxVolume:" + maxVolume);
                LoggerUtils.xd("currVolume:" + currentVolume);
                LoggerUtils.xd("streamDecibel:" + streamDecibel);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            volumeIndex, AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
                }
                //当前音量
                int changeVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                LoggerUtils.xd("2 changeVolume:" + changeVolume);

            }
        }
    }


    private void unregisterVolumeChangeReceiver() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

}
