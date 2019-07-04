package com.genialsir.tvh.db;

import android.database.Cursor;

import com.genialsir.tvh.utils.LoggerUtils;

/**
 * @author genialsir@163.com (GenialSir) on 2019/6/6
 */
public class QueryWeChatDB {

    public static String getNickname(String weChatID) {
        String sqlStr = "select nickname from rcontact where username ='" + weChatID + "'";
        Cursor cursor = getWeChatCursor(sqlStr);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("nickname");
            return cursor.getString(index);
        }
        return "";
    }

    private static Cursor getWeChatCursor(String queryString) {
        Object result;
        try {
            result = WeChatDBHelper.method.invoke(WeChatDBHelper.receiver, queryString, null);
            return (Cursor) result;
        } catch (Exception e) {
            LoggerUtils.xe("Error query: " + e.getMessage());

        }
        return null;
    }
}