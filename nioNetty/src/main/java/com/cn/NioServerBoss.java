package com.cn;

import com.cn.pool.Boss;
import com.cn.pool.NioSelectorRunnablePool;
import com.cn.pool.Worker;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * boss实现类
 * @author -琴兽-
 */
public class NioServerBoss extends AbstractNioSelector implements Boss{


    public NioServerBoss(Executor executor, String theradName, NioSelectorRunnablePool nioSelectorRunnablePool) {
        super(executor, theradName, nioSelectorRunnablePool);
    }

    @Override
    public void registerAcceptChannelTask(ServerSocketChannel serverSocketChannel) {
        final Selector selector = this.selector;
        //System.out.println(Thread.currentThread().getName() + " registerAcceptChannelTask() selector = " + this.selector);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        };
        registerTask(task);
    }

    @Override
    protected int select(Selector selector) throws IOException {
        //证明boss线程池在这里发生阻塞
        //System.out.println(Thread.currentThread().getName() + "  ---NioServerBoss select() 1-----");
        int select = selector.select();
        //System.out.println(Thread.currentThread().getName() + "  ---NioServerBoss select() 2-----");
        return select;
    }

    @Override
    protected void process(Selector selector) throws IOException {

        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        if (selectionKeys.isEmpty()) {
            return;
        }

        for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext(); ) {
            SelectionKey key = it.next();
            it.remove();

            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();
            channel.configureBlocking(false);

            System.out.println(Thread.currentThread().getName() + " " + channel.getRemoteAddress() + " 新客户端链接 ");

            // 获取一个worker
            Worker nextWorker = getNioSelectorRunnablePool().nextWorker();
            // 注册新客户端接入任务
            nextWorker.registerNewChannelTask(channel);
        }
    }

}
