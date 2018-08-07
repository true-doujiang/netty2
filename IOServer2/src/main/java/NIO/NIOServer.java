package NIO;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO服务端
 *
 * @author -琴兽-
 */
public class NIOServer {


    /**
     * 虽然只有一个main线程但是可以同时接入多个客户端，
     * OioServer中只有用了多线程才能同时接入多个客户端
     *
     * 2、selector.select();阻塞，那为什么说nio是非阻塞的IO？
     *    阻塞 非阻塞是指 读数据时 是否阻塞   channel.read(buffer); 这里并没有阻塞啊
     *
     * 3、SelectionKey.OP_WRITE是代表什么意思
     *    OP_WRITE表示底层socket缓冲区是否有空间，是 返回true
     */
    public static ExecutorService threadPool = Executors.newCachedThreadPool();

    // 通道管理器
    private Selector selector;

    /**
     * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
     * @param port 绑定的端口号
     */
    public void initServer(int port) throws IOException {
        // 获得一个ServerSocket通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        int ops = serverChannel.validOps();
        System.out.println(ops);

        // 设置通道为非阻塞
        serverChannel.configureBlocking(false);
        // 将该通道对应的ServerSocket绑定到port端口
        serverChannel.socket().bind(new InetSocketAddress(port));

        //serverChannel = sun.nio.ch.ServerSocketChannelImpl[/0:0:0:0:0:0:0:0:8000]
        System.out.println("serverChannel = " + serverChannel);

        // 获得一个通道管理器
        this.selector = Selector.open();

        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
        // 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }


    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     */
    private void listen() throws IOException {
        System.out.println(Thread.currentThread().getName() + " 服务端启动成功！");
        // 轮询访问selector
        while (true) {
            // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
            selector.select();

            // 获得selector中选中的项的迭代器，选中的项为注册的事件
            Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                // 删除已选的key,以防重复处理
                it.remove();

                //这个直接放到线程池里处理会有问题
                handler(key);

                /*threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handler(key);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
            }

        }
    }

    /**
     * 处理请求
     * @param key
     * @throws IOException
     */
    private void handler(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            // 客户端请求连接事件
            handlerAccept(key);
        } else if (key.isReadable()) {
            // 获得了可读的事件
            handlerRead(key);
        }
    }

    private void handlerAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        //server = sun.nio.ch.ServerSocketChannelImpl[/0:0:0:0:0:0:0:0:8000]
        System.out.println("server = " + server);


        // 获得和客户端连接的通道
        SocketChannel channel = server.accept();
        // 设置成非阻塞
        channel.configureBlocking(false);

        // 在这里可以给客户端发送信息哦
        System.out.println(Thread.currentThread().getName() + " 新的客户端连接");

        // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void handlerRead(SelectionKey key) throws IOException {
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();

        //channel = java.nio.channels.SocketChannel[connected local=/172.17.5.93:10101 remote=/223.104.212.189:65406]
        System.out.println("channel = " + channel);

        ByteBuffer buffer = ByteBuffer.allocate(10);

        //当len=-1 代表客户端断开连接
        int len = channel.read(buffer);
        System.out.println(Thread.currentThread().getName() + " len = " + len);
        //这里应该是循环读取， 而现在的情况是一次读取不完还会再次触发key.isReadable()到这里再次读取
        if(len > 0){
            byte[] data = buffer.array();
            String msg = new String(data).trim();
            System.out.println(Thread.currentThread().getName() + " 服务端收到信息：" + msg);

            //回写数据
            ByteBuffer outBuffer = ByteBuffer.wrap("hao de".getBytes());
            channel.write(outBuffer);// 将消息回送给客户端
        }else{
            System.out.println(Thread.currentThread().getName() + " 客户端关闭 len = " + len);
            //客户端关闭的时候会抛出异常，死循环   解决方案
            key.cancel();
        }
    }

    public static void main(String[] args) throws IOException {
        NIOServer nioServer = new NIOServer();
        nioServer.initServer(10101);
        nioServer.listen();
    }

}
