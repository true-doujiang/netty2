package com.cn;

import com.cn.pool.NioSelectorRunnablePool;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Start {

    public static void main(String[] args) {

        //初始化线程
        NioSelectorRunnablePool nioSelectorRunnablePool = new NioSelectorRunnablePool(
                                                            Executors.newCachedThreadPool(),
                                                            Executors.newCachedThreadPool()
                                                        );

        //获取服务类
        ServerBootstrap bootstrap = new ServerBootstrap(nioSelectorRunnablePool);
        bootstrap.bind(new InetSocketAddress(9898));

        System.out.println("server start ---> 9898");
    }
}
