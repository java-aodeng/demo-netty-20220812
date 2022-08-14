package com.example.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * @author AoDeng
 * @date 2022/8/12
 */
public class MessageUtil {
    /**
     * 发送消息编码
     *
     * @param message 消息体
     * @return 编码之后的消息
     */
    public static ByteBuf sendMessage(String message) {
        return Unpooled.wrappedBuffer((message).getBytes((CharsetUtil.UTF_8)));
    }

    /**
     * 接收消息编码
     *
     * @param message 消息体
     * @return 解码之后的消息
     */
    public static String getMessage(Object message) {
        return ((ByteBuf) message).toString(CharsetUtil.UTF_8);
    }
}
