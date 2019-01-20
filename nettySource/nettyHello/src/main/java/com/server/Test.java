package com.server;

import java.util.concurrent.atomic.AtomicBoolean;

public class Test {

    static AtomicBoolean wakenUp = new AtomicBoolean();

    public static void main(String[] args) {
        System.out.println(wakenUp);

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }).start();
        }
        init();
    }

    public static void init()
    {
        if(wakenUp.compareAndSet(false, true) )
        {
            // 这里放置初始化代码....
            System.out.println(Thread.currentThread().getName() + "===" + wakenUp);
        }
    }
}
