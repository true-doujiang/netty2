package com.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端消息处理
 * @author -琴兽-
 */
public class ClientHandler extends SimpleChannelInboundHandler<String>{

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(Thread.currentThread().getName()
                + " ip = " + ctx.channel().remoteAddress() + "  id = " + ctx.channel().id() + " 客户端收到消息: " + msg);
    }

}
