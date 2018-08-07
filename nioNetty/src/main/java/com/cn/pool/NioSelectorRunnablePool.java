package com.cn.pool;

import com.cn.NioServerBoss;
import com.cn.NioServerWorker;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * selector线程管理者
 * @author -琴兽-
 */
public class NioSelectorRunnablePool {

    /**
     * boss线程数组
     */
    private final AtomicInteger bossIndex = new AtomicInteger();
    private Boss[] bosses;

    /**
     * worker线程数组
     */
    private final AtomicInteger workerIndex = new AtomicInteger();
    private Worker[] workers;


    public NioSelectorRunnablePool(Executor boss, Executor worker) {
        initBoss(boss, 1); //也就是是迎接客人的selector只有一个
        initWorker(worker, Runtime.getRuntime().availableProcessors() * 2);
        //initWorker(worker, 1);
    }

    /**
     * 初始化boss线程
     * @param boss
     * @param count
     */
    private void initBoss(Executor boss, int count) {
        this.bosses = new NioServerBoss[count];
        for (int i = 0; i < bosses.length; i++) {
            this.bosses[i] = new NioServerBoss(boss, "boss-thread-" + (i + 1), this);
        }
    }

    /**
     * 初始化worker线程
     * @param worker
     * @param count
     */
    private void initWorker(Executor worker, int count) {
        this.workers = new NioServerWorker[count];
        for (int i = 0; i < workers.length; i++) {
            this.workers[i] = new NioServerWorker(worker, "worker-thread-" + (i + 1), this);
        }
    }

    /**
     * 获取一个boss
     * @return
     */
    public Boss nextBoss() {
        return bosses[Math.abs(bossIndex.getAndIncrement() % bosses.length)];
    }

    /**
     * 获取一个worker
     * @return
     */
    public Worker nextWorker() {
        return workers[Math.abs(workerIndex.getAndIncrement() % workers.length)];
    }
}
