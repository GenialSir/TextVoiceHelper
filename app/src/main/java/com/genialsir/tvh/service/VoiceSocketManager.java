package com.genialsir.tvh.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.genialsir.tvh.utils.CloseUtils;
import com.genialsir.tvh.utils.LoggerUtils;
import com.genialsir.tvh.utils.TTSUtils;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * @author genialsir@163.com (GenialSir) on 2019/6/26
 */
public class VoiceSocketManager extends Service {

    private TTSUtils ttsUtils;
    private boolean mIsServiceDestroyed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new TcpServer()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class TcpServer implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket;
            try {
                //监听本地8688端口
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                LoggerUtils.d("establish tcp server failed, port:8688");
                e.printStackTrace();
                return;
            }
            while (!mIsServiceDestroyed) {
                try {
                    //接受客户端请求
                    final Socket client = serverSocket.accept();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (Exception e) {
                    LoggerUtils.d("error " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        Context applicationContext = getApplication().getApplicationContext();
        initXF(applicationContext);
        //用于接受客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //用于向客户端发送消息
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(client.getOutputStream())), true);


        String clientContent;
        while (true) {
            clientContent = in.readLine();
            LoggerUtils.d("responseClient msg from client: " + clientContent);
            if ("Close socket".equals(clientContent)) {
                //客户端断开链接
                if (ttsUtils != null) {
                    ttsUtils.speak("我是Voice Socket Manager, 客户端请求断开链接，撒哟啦啦");
                }
                LoggerUtils.d("客户端断开链接.");
                break;
            }
            if (ttsUtils != null) {
                ttsUtils.speak(clientContent);
            }else {
                LoggerUtils.e("ttsUtils is null.");
            }
        }
        LoggerUtils.d("client quit.");
        //关闭流
        CloseUtils.closeQuietly(printWriter);
        CloseUtils.closeQuietly(in);
        client.close();
    }


    private void initXF(Context context) {
        SpeechUtility.createUtility(context, "appid=5d07631c");
        Setting.setShowLog(true);
        ttsUtils = TTSUtils.getInstance(context);
        ttsUtils.init();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed = true;
        super.onDestroy();
    }

}
