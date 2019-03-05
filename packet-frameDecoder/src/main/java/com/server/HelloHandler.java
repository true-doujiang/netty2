package com.server;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class HelloHandler extends SimpleChannelHandler {

    /**
     * 管道的整个处理过程：从解码到处理都是worker的一个线程，所以这里不会有并发问题
     *
     * 多次请求count还是从1开始的？？？
     */
    private int count = 1;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        System.out.println(e.getMessage() + "  " + count);
        count++;
    }
}
