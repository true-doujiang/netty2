package com.heart;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateEvent;

/*
//看下IdleStateAwareChannelHandler源码  还是使用下面直接  extends SimpleChannelHandler的方式
public class HelloHandler extends IdleStateAwareChannelHandler implements ChannelHandler {

	
	
	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		System.out.println(e.getState() + "   " + sdf.format(new Date())); 
	}
}*/


public class HelloHandler extends SimpleChannelHandler {

	SimpleDateFormat sdf = new SimpleDateFormat("ss");
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		System.out.println(e.getMessage());
	}

	//根据IdleStateAwareChannelHandler类写的
	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof IdleStateEvent) {
			
			//System.out.println(((IdleStateEvent) e).getState() + "   " + sdf.format(new Date())); 
			
			if(((IdleStateEvent)e).getState() == IdleState.ALL_IDLE){
				System.out.println("提玩家下线");
				
				//关闭会话,踢玩家下线   直接关闭的话太暴力了
				ChannelFuture write = ctx.getChannel().write("time out, you will close");
				
				write.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						 ctx.getChannel().close();
					}
				});
			}
		} else {
			super.handleUpstream(ctx, e);
		}
	}
}
