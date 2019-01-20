package com.heart;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
/**
 * netty服务端入门
 * @author -琴兽-
 */
public class Server {

    /**
     * 本demo只是检测客户单有没有读写数据，并不是发送心跳数据报
     */
	public static void main(String[] args) {

		ServerBootstrap bootstrap = new ServerBootstrap();

		ExecutorService boss = Executors.newCachedThreadPool();
		ExecutorService worker = Executors.newCachedThreadPool();
		bootstrap.setFactory(new NioServerSocketChannelFactory(boss, worker));
		
		//netty的定时器
		final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();
		
		//设置管道的工厂
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {

				ChannelPipeline pipeline = Channels.pipeline();
				//添加检测会话状态的定时器   时间站在服务器的角度 readerIdleTimeSeconds: server这么久没有收到数据就出发
				pipeline.addLast("idle", new IdleStateHandler(hashedWheelTimer, 8, 11, 30));
				pipeline.addLast("decoder", new StringDecoder());
				pipeline.addLast("encoder", new StringEncoder());
				pipeline.addLast("helloHandler", new HelloHandler());
				return pipeline;
			}
		});
		
		bootstrap.bind(new InetSocketAddress(9898));
		
		System.out.println("start!!!");
	}

}
