package com.cn;

import com.cn.pool.NioSelectorRunnablePool;
import com.cn.pool.Worker;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;


/**
 * worker实现类
 * @author -琴兽-
 */
public class NioServerWorker extends AbstractNioSelector implements Worker{


    public NioServerWorker(Executor executor, String threadName, NioSelectorRunnablePool nioSelectorRunnablePool) {
        super(executor, threadName, nioSelectorRunnablePool);
    }

    /**
     * 加入一个新的socket客户端
     */
    @Override
    public void registerNewChannelTask(SocketChannel socketChannel) {
        final Selector selector = this.selector;
        //这个方法会被boss线程池调用，所以当前线程是boss线程池的
        //System.out.println(Thread.currentThread().getName() + " registerNewChannelTask() selector = " + this.selector);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    //将客户端注册到selector中
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        };
        registerTask(task);
    }

    @Override
    protected int select(Selector selector) throws IOException {
        //System.out.println(Thread.currentThread().getName() + "  ---NioServerWorker select() ==1== ");
        int select = selector.select();
        //System.out.println(Thread.currentThread().getName() + "  ---NioServerWorker select() ==2== ");
        return select;
    }

    @Override
    protected void process(Selector selector) throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        if (selectionKeys.isEmpty()) {
            return;
        }

        Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            // 移除，防止重复处理
            it.remove();

            // 得到事件发生的Socket通道
            SocketChannel channel = (SocketChannel) key.channel();

            // 数据总长度
            int len = 0;
            boolean failure = true;
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //读取数据
            try {
                len = channel.read(buffer);
                failure = false;
            } catch (Exception e) {
                //客户端断开时会有异常信息
                //e.printStackTrace();
            }

            //判断是否连接已断开
            if (len <= 0 || failure) {
                key.cancel();
                System.out.println(Thread.currentThread().getName() + " 客户端 " + channel.getRemoteAddress()+ " 断开连接");
            } else {
                System.out.println(Thread.currentThread().getName() + " 收到数据:" + new String(buffer.array(), 0, len));
                //回写数据
                ByteBuffer outBuffer = ByteBuffer.wrap("server received data!!\n".getBytes());
                channel.write(outBuffer);// 将消息回送给客户端
            }
        }
    }

}
