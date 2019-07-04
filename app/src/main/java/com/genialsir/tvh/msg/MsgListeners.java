package com.genialsir.tvh.msg;

import android.database.Cursor;
import android.os.SystemClock;
import android.text.TextUtils;

import com.genialsir.tvh.db.QueryWeChatDB;
import com.genialsir.tvh.provider.VolumeProvider;
import com.genialsir.tvh.utils.CloseUtils;
import com.genialsir.tvh.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/5
 */
public class MsgListeners {

    private static Socket mClientSocket;
    private static PrintWriter mClientPrintWriter;

    public static void listenerPath(Cursor cursor, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //主动发送的status=2,接收的为3
        int status = WeChatMessage.getStatus(cursor);
        WeChatMessage.Type msgType = WeChatMessage.getType(cursor);

        //建立Client端
        if (mClientPrintWriter == null) {
            new Thread() {
                @Override
                public void run() {
                    connectTCPServer();
                }
            }.start();
        } else {
            LoggerUtils.xd("mClientPrintWriter is " + mClientPrintWriter);
        }
        switch (msgType) {
            case TEXT_MESSAGE:
                List<TextMessage> textMessages = WeChatMessage.getTextMessage(cursor);

                //初始化监测音量键提供者。
                VolumeProvider volumeProvider = new VolumeProvider(loadPackageParam);

                for (TextMessage textMessage : textMessages) {
                    if (textMessage == null) {
                        continue;
                    }
                    if (textMessage.getCreateTime() > WeChatMessage.lastSend) {
                        WeChatMessage.lastSend = textMessage.getCreateTime();
                        //处理转发的消息
                    }

                    SystemClock.sleep(1000);
                    String textContent = QueryWeChatDB.getNickname(textMessage.getFromUser())
                            + " 来消息:  " + textMessage.getContent();
                    LoggerUtils.xd("GenialSir Msg textContent " + textContent);

                    if (!TextUtils.isEmpty(textContent) && mClientPrintWriter != null) {
                        mClientPrintWriter.println(textContent);
                        LoggerUtils.xd("GenialSir mClientPrintWriter textContent " + textContent);
                    } else {
                        LoggerUtils.xd("mClientPrintWriter is null.. ");
                        new Thread() {
                            @Override
                            public void run() {
                                connectTCPServer();
                            }
                        }.start();
                    }

                    //当前发送者的ID。
                    volumeProvider.setCurrentWeChatID(textMessage.getFromUser());

                    //系统API语音播报
//                    DogInterpret.getInstance(
//                            WeChatHelperUtils.getContext(loadPackageParam.classLoader))
//                            .init()
//                            .toInterpret(textContent);

                }
                break;
            case VIDEO_MESSAGE:
                break;
            case IMG_MESSAGE:
                break;
            default:
                break;
        }
    }

    private static void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mClientPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);

                LoggerUtils.xd("genial sir connect tcp server success.");
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                LoggerUtils.xd("genial sir connect tcp server failed, retry...");
            }
        }

        try {
            //接受服务器端的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                LoggerUtils.xd("genial sir receive br : " + br);
                String msg = br.readLine();
                LoggerUtils.xd("genial sir receive : " + msg);
                if (msg.contains("Close Socket service")) {
                    break;
                }
            }
            LoggerUtils.xd("genial sir quit...");
            CloseUtils.closeQuietly(mClientPrintWriter);
            CloseUtils.closeQuietly(br);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
