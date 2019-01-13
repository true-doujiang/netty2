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


    /**
     *
     * @param executor
     * @param theradName
     * @param nioSelectorRunnablePool
     */
    public NioServerBoss(Executor executor, String theradName, NioSelectorRunnablePool nioSelectorRunnablePool) {
        super(executor, theradName, nioSelectorRunnablePool);
    }

    /**
     * 加入一个新的 ServerSocketChannel
     *
     * 这个方法会被main线程调用,只是启动的时候调用1次
     */
    @Override
    public void registerAcceptChannelTask(ServerSocketChannel serverSocketChannel) {
        final Selector selector = this.selector;
        //System.out.println(Thread.currentThread().getName() + " registerAcceptChannelTask() selector = " + this.selector);

        /**
         * 这个任务是 ServerSocketChannel的连接事件
         */
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

        // 想boss线程组中添加一个任务
        registerTask(task);
    }

    @Override
    protected int select(Selector selector) throws IOException {
        //证明boss线程池在这里发生阻塞
       // System.out.println(Thread.currentThread().getName() + "  ---NioServerBoss select() 1-----" + System.currentTimeMillis());
        int select = selector.select();
       // System.out.println(Thread.currentThread().getName() + "  ---NioServerBoss select() 2-----" + System.currentTimeMillis());
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

            System.out.println(Thread.currentThread().getName() + "  boss迎接新客人: " + channel);

            /**
             * boss线程 获取一个worker 并把server接收到的客户端SocketChannel扔给worker线程组就完事了
             * 以后SocketChannel的读写就由worker线程取执行了。
             */
            Worker nextWorker = getNioSelectorRunnablePool().nextWorker();
            nextWorker.registerNewChannelTask(channel);
        }
    }

}
