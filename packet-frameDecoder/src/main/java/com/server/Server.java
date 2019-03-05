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

                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new MyDecoder());
                pipeline.addLast("handler1", new HelloHandler());
                return pipeline;
            }
        });

        bootstrap.bind(new InetSocketAddress(8088));

        System.out.println("start!!!");
    }

    /**
     三月 05, 2019 9:51:38 下午 org.jboss.netty.channel.SimpleChannelHandler
     警告: EXCEPTION, please implement com.server.HelloHandler.exceptionCaught() for proper handling.
     java.lang.IndexOutOfBoundsException: Not enough readable bytes - Need 4, maximum is 0

     */

}
