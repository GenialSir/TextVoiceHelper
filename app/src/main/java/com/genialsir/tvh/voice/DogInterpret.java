package com.genialsir.tvh.voice;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import com.genialsir.tvh.R;
import com.genialsir.tvh.utils.LoggerUtils;

import java.util.Locale;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/6
 */
public class DogInterpret {

    private static Context mContext;
    private TextToSpeech textToSpeech;
    private String msgContent;

    private DogInterpret() {

    }

    public static DogInterpret getInstance(Context context) {
        mContext = context;
        return DogInterpretHolder.sInstance;
    }

    private static class DogInterpretHolder {
        private static final DogInterpret sInstance = new DogInterpret();
    }


    public DogInterpret init() {
        if (mContext != null) {
            textToSpeech = new TextToSpeech(mContext, onInitListener);
        } else {
            LoggerUtils.d("DogInterpret mContext is null.");
        }
        return this;
    }

    public void toInterpret(final String content) {
        msgContent = content;
        //延迟
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                    // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.setPitch(1.5f);
                    //设定语速 ，默认1.0正常语速
                    textToSpeech.setSpeechRate(0.8f);
                    //朗读，注意这里三个参数的added in API level 4   四个参数的added in API level 21
                    textToSpeech.speak(msgContent, TextToSpeech.QUEUE_FLUSH, null);
                    LoggerUtils.d("DogInterpret msgContent is " + msgContent);
                }
            }
        }, 300);

    }


    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            LoggerUtils.d("status = " + status);
            switch (status) {
                case TextToSpeech.SUCCESS:
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        LoggerUtils.d(mContext.getString(R.string.data_error));
                    }
                    break;
                case TextToSpeech.ERROR:
                    LoggerUtils.d("Error DogInterpret msgContent is " + msgContent);
                    break;
                default:
                    break;
            }

        }
    };

    public void stopVoice() {
//        打断正在朗读的TTS
        textToSpeech.stop();
//        关闭释放资源
        textToSpeech.shutdown();
    }

}
