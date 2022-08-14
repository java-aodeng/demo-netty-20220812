package com.example.netty.handle;

import com.alibaba.fastjson.JSONObject;
import com.example.netty.config.NettyConfig;
import com.example.netty.util.MessageUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author AoDeng
 * @date 2022/8/12
 */
@Component
@ChannelHandler.Sharable
public class NettyServerEventHandle extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(NettyServerEventHandle.class);

    /**
     * 事件监听
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //获取当前信道状态
        IdleStateEvent event = (IdleStateEvent) evt;
        //连接客户端ID
        String channelId = ctx.channel().id().toString();
        switch (event.state()) {
            //读空闲
            case READER_IDLE:
                break;
            //写空闲
            case WRITER_IDLE:
                break;
            //读写空闲
            case ALL_IDLE:
                log.info("服务端向客户端ID:{}发送心跳数据..", channelId);
                ctx.writeAndFlush(MessageUtil.sendMessage(JSONObject.toJSONString(NettyConfig.SEND_HEAR_MESSAGE)));
                break;
            default:
                break;
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 接收到消息的时候触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //消息体
            String info = MessageUtil.getMessage(msg);
            //连接客户端ID
            String channelId = ctx.channel().id().toString();
            log.info("（处理前）收到ID:{}发来的消息:{}", channelId, info+" : "+info.length());

            //TODO 消息体根据具体业务处理转换...

            log.info("（处理后）收到客户端ID:{}发来的消息:{}", channelId, info+" : "+info.length());
            //处理业务开始...

            //TODO 收到消息后处理具体业务...

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            ctx.writeAndFlush(MessageUtil.sendMessage(JSONObject.toJSONString(NettyConfig.RESPONSE_ERROR_MESSAGE)));
        }
    }


    /**
     * 绑定
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //获取客户端IP地址进行身份验证
        String ipAddress = ctx.channel().remoteAddress().toString();
        String channelId = ctx.channel().id().toString();
        log.info("客户端ID:{},IP地址:{}已经成功连接....", channelId, ipAddress);
    }

    /**
     * 取消绑定
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    /**
     * 抛出异常的时候触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String channelId = ctx.channel().id().toString();
        String ipAddress = ctx.channel().remoteAddress().toString();
        log.error("客户端ID:{},IP地址:{}发生了异常....", channelId, ipAddress);
        super.exceptionCaught(ctx, cause);
    }
}
