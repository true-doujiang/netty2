package com.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class Server {

    public static void main(String[] args) {
        //服务类
        ServerBootstrap bootstrap = new ServerBootstrap();

        //boss线程监听端口，worker线程负责数据读写
        ExecutorService boss = Executors.newCachedThreadPool();
        ExecutorService worker = Executors.newCachedThreadPool();

        //设置niosocket工厂
        bootstrap.setFactory(new NioServerSocketChannelFactory(boss, worker));

        //设置管道的工厂
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {

                MyHandler1 handler1 = new MyHandler1();
                MyHandler2 handler2 = new MyHandler2();
                System.out.println("handler1=" + handler1 + "  handler2=" + handler2);

                ChannelPipeline pipeline = Channels.pipeline();
                System.out.println("pipeline=" + pipeline);

                pipeline.addLast("handler1", handler1);
                pipeline.addLast("handler2", handler2);
                return pipeline;
            }
        });

        bootstrap.bind(new InetSocketAddress(8088));

        System.out.println("start!!!");
    }

}
