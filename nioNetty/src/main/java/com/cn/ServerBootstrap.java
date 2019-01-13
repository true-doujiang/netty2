package com.cn;

import com.cn.pool.Boss;
import com.cn.pool.NioSelectorRunnablePool;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * 服务类
 * @author -琴兽-
 */
public class ServerBootstrap {

    /**
     * selector线程管理者
     */
    private NioSelectorRunnablePool nioSelectorRunnablePool;

    /**
     * 构造器
     */
    public ServerBootstrap(NioSelectorRunnablePool nioSelectorRunnablePool) {
        this.nioSelectorRunnablePool = nioSelectorRunnablePool;
    }

    /**
     * 绑定端口
     * @param localAddress
     */
    public void bind(SocketAddress localAddress) {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(localAddress);

            //System.out.println(Thread.currentThread().getName() + " bind()==> nioSelectorRunnablePool = "  + nioSelectorRunnablePool);

            //获取一个boss线程
            Boss nextBoss = nioSelectorRunnablePool.nextBoss();
            //向boss注册一个ServerSocket通道
            nextBoss.registerAcceptChannelTask(serverChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
