package com.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * netty服务端入门
 * @author -琴兽-
 */
public class Server {

    public static void main(String[] args) {

        ServerBootstrap bootstrap = new ServerBootstrap();

        ExecutorService boss = Executors.newCachedThreadPool();
        // 处理读写
        ExecutorService worker = Executors.newCachedThreadPool();

        bootstrap.setFactory(new NioServerSocketChannelFactory(boss, worker));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                // 上行过滤器 Server接收数据时经过
                pipeline.addLast("decoder", new StringDecoder());
                // 下行过滤器 Server发送数据时经过
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast("helloHandler", new HelloHandler());
                return pipeline;
            }
        });

        bootstrap.bind(new InetSocketAddress(9898));

        System.out.println("start !! ");
    }
}
