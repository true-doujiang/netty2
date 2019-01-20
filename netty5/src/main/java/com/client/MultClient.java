package com.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 多连接客户端
 * @author -琴兽-
 */
public class MultClient {

    /**
     * 服务类
     */
    private Bootstrap bootstrap = new Bootstrap();

    /**
     * 会话
     */
    private List<Channel> channels = new ArrayList<>();

    /**
     * 引用计数
     */
    private final AtomicInteger index = new AtomicInteger();

    /**
     * 初始化
     * @param count
     */
    public void init(int count) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        bootstrap.group(worker);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new StringEncoder());
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        for (int i=0; i<count; i++) {
            ChannelFuture future = bootstrap.connect("127.0.0.1", 9898);
            channels.add(future.channel());
        }
    }



    /**
     * 获取会话
     * @return
     */
    public Channel nextChannel(){
        return getFirstActiveChannel(0);
    }

    private Channel getFirstActiveChannel(int count){
        Channel channel = channels.get(Math.abs(index.getAndIncrement() % channels.size()));

        if(!channel.isActive()){
            //重连
            reconnect(channel);
            if(count >= channels.size()){
                throw new RuntimeException("no can use channel");
            }
            return getFirstActiveChannel(count + 1);
        }

        return channel;
    }

    /**
     * 重连
     * @param channel
     */
    private void reconnect(Channel channel){
        //用锁不是，用单线程的任务队列
        synchronized(channel){
            if(channels.indexOf(channel) == -1){
                return ;
            }
            Channel newChannel = bootstrap.connect("127.0.0.1", 10101).channel();
            channels.set(channels.indexOf(channel), newChannel);
        }
    }
}
