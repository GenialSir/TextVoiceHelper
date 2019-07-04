package com.genialsir.tvh.msg;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/6
 */
public class WeChatMessage {

    private long createTime;
    private String fromUser;
    //null means send by myself or event.
    private String id;
    private String content;
    private static WeChatMessage last = null;
    public static long lastSend = -1;

    public WeChatMessage(long createTime, String fromUser, String id, String content) {
        this.createTime = createTime;
        this.fromUser = fromUser;
        this.id = id;
        this.content = content;
    }

    public static int getStatus(Cursor cursor) {
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("status");
            return cursor.getInt(index);
        }
        return -1;
    }

    public String getId() {
        return id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getContent() {
        return content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public static WeChatMessage.Type getType(Cursor cursor) {
        Type type = Type.UNKNOWN;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("type");
            if (index != -1) {
                int typeInt = cursor.getInt(index);
                switch (typeInt) {
                    case 1:
                    case 11:
                    case 21:
                    case 31:
                    case 36:
                        type = Type.TEXT_MESSAGE;
                        break;
                    default:
                        break;
                }
            }
        }
        return type;
    }


    public static List<TextMessage> getTextMessage(Cursor cursor) {
        List<TextMessage> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                long createTime = cursor.getLong(cursor.getColumnIndex("createTime"));
                String fromUser = cursor.getString(cursor.getColumnIndex("talker"));
                String id = cursor.getString(cursor.getColumnIndex("msgId"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String[] ss = content.split(":\n");
                TextMessage textMessage;
                if (ss.length != 2) {
                    textMessage = new TextMessage(createTime, fromUser, id, content);
                } else {
                    //群消息处理
                    textMessage = new TextMessage(fromUser, createTime, ss[0], id, ss[1]);
                }
                //ignore duplicated messages
                if (last != null && last.createTime >= textMessage.getCreateTime()) {
                    continue;
                }
                last = textMessage;
                result.add(textMessage);
            } while (cursor.moveToNext());
        }
        return result;
    }

    //SUBSCRIBE订阅号消息
    public enum Type {
        TEXT_MESSAGE, AUDIO_MESSAGE, EVENT, UNKNOWN, SUBSCRIBE, IMG_MESSAGE,
        VIDEO_MESSAGE, EMOJI, ARTICLE_LINK
    }

}
