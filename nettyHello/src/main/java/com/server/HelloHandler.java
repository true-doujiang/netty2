package com.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

/**
 * 消息接受处理类
 * @author -琴兽-
 */
public class HelloHandler extends SimpleChannelHandler {

    //这里只重写了5个方法  其它的自己看看

    /**
     * 接收消息
     *
     *
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        System.out.println(Thread.currentThread().getName() + " messageReceived");

        //int i = 1/0;

        //接收数据
        //ChannelBuffer cb = (ChannelBuffer) e.getMessage();
        //System.out.println(new String(cb.array()));

        String message = (String) e.getMessage();
        System.out.println(message);

        //回写数据
        //没有添加StringEncoder之前
        //ChannelBuffer hi = ChannelBuffers.copiedBuffer("hi hi;".getBytes());
        //ctx.getChannel().write(hi);

        ctx.getChannel().write("hi hi;");


        super.messageReceived(ctx, e);
    }

    /**
     * 捕获异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        System.out.println(Thread.currentThread().getName() + " exceptionCaught");
        super.exceptionCaught(ctx, e);
    }

    /**
     * 新连接
     * 应用场景：通常用来检测IP是否是黑名单   也可以在前面加个过滤器
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println(Thread.currentThread().getName() + " channelConnected");
        super.channelConnected(ctx, e);
    }

    /**
     * 必须是链接已经建立，关闭通道的时候才会触发
     * 应用场景：可以在用户断线的时候清除用户的缓存数据...
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println(Thread.currentThread().getName() + " channelDisconnected");
        super.channelDisconnected(ctx, e);
    }

    /**
     * 连接不一定建立了
     * channel关闭的时候触发
     */
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println(Thread.currentThread().getName() + " channelClosed");
        super.channelClosed(ctx, e);
    }
}
