package com.genialsir.tvh.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author genialsir@163.com (GenialSir) on 2017/4/21
 */
public class CloseUtils {
    private CloseUtils(){}

    public static void closeQuietly(Closeable closeable){
        if(null != closeable){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
