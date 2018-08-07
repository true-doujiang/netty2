package com.cn.pool;


import java.net.ServerSocket;
import java.nio.channels.SocketChannel;

/**
 * worker接口
 * @author -琴兽-
 */
public interface Worker {


    /**
     * 加入一个新的客户端会话
     * @param socketChannel
     */
    public void registerNewChannelTask(SocketChannel socketChannel);
}
