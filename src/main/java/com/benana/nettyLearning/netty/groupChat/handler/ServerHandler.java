package com.benana.nettyLearning.netty.groupChat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Benana
 * @date 2023/4/28 15:25
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端（" + ctx.channel().remoteAddress() + "）已连接...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("收到消息：" + msg);
    }
}
