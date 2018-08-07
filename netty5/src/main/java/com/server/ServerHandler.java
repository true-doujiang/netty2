package com.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 服务端消息处理
 * @author -琴兽-
 */
public class ServerHandler extends SimpleChannelInboundHandler<String>{


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {

        System.out.println(Thread.currentThread().getName() + " ip = " + ctx.channel().remoteAddress() + "  id = " + ctx.channel().id() + " 收到消息: " + msg);
        //底层调用的是同一个方法
        ctx.channel().writeAndFlush("hi");
        ctx.writeAndFlush("hi");
    }

    /**
     * 新客户端接入
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(Thread.currentThread().getName() + " ip = " + ctx.channel().remoteAddress() + "  id = " + ctx.channel().id());
        System.out.println("channelActive");
    }

    /**
     * 客户端断开
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(Thread.currentThread().getName() + " ip = " + ctx.channel().remoteAddress() + "  id = " + ctx.channel().id());
        System.out.println("channelInactive 客户端断开");
    }

    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }



}
