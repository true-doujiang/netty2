package com.heart;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 服务端消息处理
 * @author -琴兽-
 *
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    /**
     * netty5修改了netty3的API
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt instanceof IdleStateEvent){

            IdleStateEvent event = (IdleStateEvent)evt;

            System.out.println(event.state() + "   " + sdf.format(new Date()));

            if(event.state() == IdleState.ALL_IDLE){
                System.out.println("提玩家下线");
                //清除超时会话
                ChannelFuture writeAndFlush = ctx.writeAndFlush("time out. you will close");
                // TODO 确认客户单收到了消息后就出发这个事件监听器，关闭channel
                writeAndFlush.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        ctx.channel().close();
                    }
                });
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
	protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(sdf.format(new Date()) + " 收到: " + msg);

		ctx.channel().writeAndFlush("hi");
		ctx.writeAndFlush("hi");
	}


	/**
	 * 新客户端接入
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(sdf.format(new Date()) + " == " + " channelActive");
	}

}
