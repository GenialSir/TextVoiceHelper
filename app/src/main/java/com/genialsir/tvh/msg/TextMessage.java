package com.genialsir.tvh.msg;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/6
 */
public class TextMessage extends WeChatMessage {
    public String chatRoomID = "";

    public TextMessage(String chatRoomID, long createTime, String talker,
                       String id, String content) {
        super(createTime, talker, id, content);
        this.chatRoomID = chatRoomID;
    }

    public TextMessage(long createTime, String talker, String id, String content) {
        super(createTime, talker, id, content);
    }
}
