package com.heart;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.netty.channel.*;
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

	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    /**
     * 新连接
     * 应用场景：通常用来检测IP是否是黑名单   也可以在前面加个过滤器
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println(sdf.format(new Date()) + " == " + " channelConnected");
        super.channelConnected(ctx, e);
    }

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		System.out.println(sdf.format(new Date()) + " 收到: " + e.getMessage());
	}

    /**
     * 根据IdleStateAwareChannelHandler类写的
     */
	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

	    if (e instanceof IdleStateEvent) {
			
			System.out.println(((IdleStateEvent) e).getState() + "   " + sdf.format(new Date()));

			if(((IdleStateEvent)e).getState() == IdleState.ALL_IDLE){
				System.out.println("提玩家下线");

				//关闭会话,踢玩家下线   直接关闭的话太暴力了
				ChannelFuture write = ctx.getChannel().write("time out, you will close");
                // TODO 确认客户单收到了消息后就出发这个事件监听器，关闭channel
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
