package OIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 传统socket服务端
 * @author -琴兽-
 *
 */
public class OioServer {


    /**
     * telnet 127.0.0.1 10101
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        //创建socket服务,监听10101端口
        ServerSocket server = new ServerSocket(10101);

        //server = ServerSocket[addr=0.0.0.0/0.0.0.0,localport=10101]
        System.out.println(" server = " + server);
        System.out.println(Thread.currentThread().getName() + " 服务器启动！");


        while (true) {
            //获取一个套接字（阻塞1）   在eclipse要加final修饰呢
            final Socket socket = server.accept();

            //socket = Socket[addr=/223.104.212.189,port=57450,localport=10101]
            //socket = Socket[addr=/127.0.0.1,port=1750,localport=10101]
            System.out.println(" socket = " + socket);
            System.out.println(Thread.currentThread().getName() + " 来个一个新客户端！");

            //不用线程池的话就只能为一个客户端服务，因为 handler里无线循环，inputStream.read(bytes)又阻塞
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //业务处理
                    handler(socket);
                }
            });
        }
    }

    /**
     * 读取数据
     * @param socket
     * @throws Exception
     */
    private static void handler(Socket socket) {
        try {
            byte[] bytes = new byte[1024];
            InputStream in = socket.getInputStream();

            while (true) {
                //读取数据（阻塞2）
                int len = in.read(bytes);
                System.out.println(Thread.currentThread().getName() + " read = " + len);
                if (len != -1) {
                    System.out.println(Thread.currentThread().getName() + " " + new String(bytes, 0, len));
                } else {
                    System.out.println(Thread.currentThread().getName() + " break read = " + len);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println(Thread.currentThread().getName() + " socket关闭");
                socket.close();
            } catch (IOException e) {
                System.out.println(Thread.currentThread().getName() + " socket关闭异常");
                e.printStackTrace();
            }
        }
    }
}
