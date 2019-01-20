package com.cn;

import com.cn.pool.NioSelectorRunnablePool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象selector线程类
 *
 * @author -琴兽-
 */
public abstract class AbstractNioSelector implements Runnable{

    /**
     * 线程池
     */
    private final Executor executor;


    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 线程管理对象
     */
    protected NioSelectorRunnablePool nioSelectorRunnablePool;

    /**
     * 选择器
     */
    protected Selector selector;

    /**
     * 选择器wakenUp状态标记
     */
    protected final AtomicBoolean wakenUp = new AtomicBoolean();

    /**
     * 任务队列
     */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedDeque<Runnable>();



    public AbstractNioSelector(Executor executor, String threadName, NioSelectorRunnablePool nioSelectorRunnablePool) {
        this.executor = executor;
        this.threadName = threadName;
        this.nioSelectorRunnablePool = nioSelectorRunnablePool;

        //直接启动
        openSelector();
    }

    /**
     * 获取selector并启动线程
     */
    private void openSelector() {
        try {
            this.selector = Selector.open();

            String obj = this.getClass().getSimpleName().contains("Worker") ? "NioServerWorker" : "NioServerBoss";
            System.out.println(Thread.currentThread().getName() + " 创建" + obj + " " + this + " 用 选择器 = " + this.selector);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a selector.");
        }
        //加入到boss或者worker线程池中
        this.executor.execute(this);
    }

    /**
     * 本类new出来后就直接放到线程池中执行了
     */
    @Override
    public void run() {
        Thread.currentThread().setName(this.threadName);
        while (true) {
            try {
                wakenUp.set(false);

                /**
                 *  子类实现
                 */
                int count = select(this.selector);
                System.out.println(Thread.currentThread().getName() + "  " + this + "  count= " + count);

                processTaskQueue();

                /**
                 *  子类实现
                 *  对于boss处理客户端接入    对于worker处理客户端读写
                 */
                process(this.selector);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 添加一个任务并激活selector
     * @param task
     */
    protected final void registerTask(Runnable task) {
        taskQueue.add(task);
        System.out.println(Thread.currentThread().getName() + "  添加一个 task= " + task + " ==》 " + this + "[taskQueue]= " + taskQueue);

        Selector selector = this.selector;

        if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                /**
                 * 激活run() 里的 int count = select(this.selector);
                 * 当前有selector.select()阻塞，就直接返回，
                 * 当前没有selector.select()阻塞，则下次谁调用selector.select()也不用阻塞，直接返回
                 */
                selector.wakeup();
            }
        } else {
            taskQueue.remove(task);
        }
    }

    /**
     * 取出一个任务，队列里的任务
     */
    private void processTaskQueue() {
        for ( ; ; ) {
            final Runnable task = taskQueue.poll();
            System.out.println(Thread.currentThread().getName() + "  从taskQueue = " + taskQueue + " 里取出一个任务： task = " + task);

            if (task == null) {
                break;
            }
            //直接调用run()， 那队列中放入其他对象也可以喽
            task.run();
        }
    }

    /**
     * select抽象方法, 具体怎么处理看是boss还是worker了
     * @param selector
     * @return
     * @throws IOException
     */
    protected abstract int select(Selector selector) throws IOException;

    /**
     * selector的业务处理,
     * 具体怎么处理具体怎么处理看是
     * boss[ServerSocketChannel] 负责客户端的接入
     * worker[SocketChannel] 负责读写
     * @param selector
     * @throws IOException
     */
    protected abstract void process(Selector selector) throws IOException;

    /**
     * 获取线程管理对象
     * @return
     */
    public NioSelectorRunnablePool getNioSelectorRunnablePool() {
        return nioSelectorRunnablePool;
    }
}
