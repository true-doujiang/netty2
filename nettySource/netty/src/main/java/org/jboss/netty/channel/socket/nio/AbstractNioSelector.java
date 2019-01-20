/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.nio;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.internal.DeadLockProofWorker;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ConcurrentModificationException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractNioSelector implements NioSelector {

    private static final AtomicInteger nextId = new AtomicInteger();

    private final int id = nextId.incrementAndGet();

    /**
     * Internal Netty logger.
     */
    protected static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractNioSelector.class);

    private static final int CLEANUP_INTERVAL = 256; // XXX Hard-coded value, but won't need customization.

    /**
     * Executor used to execute {@link Runnable}s such as channel registration
     * task.
     */
    private final Executor executor;

    /**
     * If this worker has been started thread will be a reference to the thread
     * used when starting. i.e. the current thread when the run method is executed.
     */
    protected volatile Thread thread;

    /**
     * Count down to 0 when the I/O thread starts and {@link #thread} is set to non-null.
     */
    final CountDownLatch startupLatch = new CountDownLatch(1);

    /**
     * The NIO {@link Selector}.
     */
    protected volatile Selector selector;

    /**
     * Boolean that controls determines if a blocked Selector.select should
     * break out of its selection process. In our case we use a timeone for
     * the select method and the select method will block for that time unless
     * waken up.
     */
    protected final AtomicBoolean wakenUp = new AtomicBoolean();

    /**
     *
     */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

    // should use AtomicInteger but we just need approximation
    private volatile int cancelledKeys;

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private volatile boolean shutdown;


    AbstractNioSelector(Executor executor) {
        this(executor, null);
    }

    AbstractNioSelector(Executor executor, ThreadNameDeterminer determiner) {
        this.executor = executor;
        openSelector(determiner);
    }

    /**
     * Start the {@link AbstractNioWorker} and return the {@link Selector} that will be used for
     * the {@link AbstractNioChannel}'s when they get registered
     */
    private void openSelector(ThreadNameDeterminer determiner) {
        try {
            /**
             * 给Boss或者Worker初始化一个Selector
             */
            selector = SelectorUtil.open();

            String obj = this.getClass().getSimpleName().contains("Worker") ? "NioWorker" : "NioServerBoss";
            System.out.println(Thread.currentThread().getName() + " 创建"+ obj + " = " + this + " 的 selector = " + selector);
        } catch (Throwable t) {
            throw new ChannelException("Failed to create a selector.", t);
        }

        /**
         *  Start the worker thread with the new Selector.
         *  启动当前Worker线程
         */
        boolean success = false;
        try {
            DeadLockProofWorker.start(executor, newThreadRenamingRunnable(id, determiner));
            success = true;
        } finally {
            if (!success) {
                // Release the Selector if the execution fails.
                try {
                    selector.close();
                } catch (Throwable t) {
                    logger.warn("Failed to close a selector.", t);
                }
                selector = null;
                // The method will return to the caller at this point.
            }
        }
        assert selector != null && selector.isOpen();
    }

    /**
     *  添加一个任务
     * @param channel
     * @param future
     */
    public void register(Channel channel, ChannelFuture future) {
        Runnable task = createRegisterTask(channel, future);
        System.out.println(Thread.currentThread().getName() + " 添加一个task: " + task);

        registerTask(task);
    }

    /**
     * 添加一个任务
     *      启动的时候 Bosss之所以能跳过select() 就是因为main线程调用了这个方法
     *
     * @param task
     */
    protected final void registerTask(Runnable task) {
        taskQueue.add(task);

        Selector selector = this.selector;

        if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                System.out.println(Thread.currentThread().getName() + " registerTask(task) - 调用" + selector + "的wakeup()");
                selector.wakeup();
            }
        } else {
            if (taskQueue.remove(task)) {
                // the selector was null this means the Worker has already been shutdown.
                throw new RejectedExecutionException("Worker has already been shutdown");
            }
        }
    }

    /**
     *
     */
    public void run() {
        thread = Thread.currentThread();
        startupLatch.countDown();

        int selectReturnsImmediately = 0;
        Selector selector = this.selector;

        if (selector == null) {
            return;
        }
        // use 80% of the timeout for measure
        final long minSelectTimeout = SelectorUtil.SELECT_TIMEOUT_NANOS * 80 / 100;
        boolean wakenupFromLoop = false;

        for (;;) {
            /**
             *  第一
             */
            System.out.println(Thread.currentThread().getName() + " wakenUp");
            wakenUp.set(false);

            try {
                long beforeSelect = System.nanoTime();

                /**
                 * 第二
                 */
                System.out.println(Thread.currentThread().getName() + " select 1");
                int selected = select(selector);
                System.out.println(Thread.currentThread().getName() + " select2= " + selected);

                if (selected == 0 && !wakenupFromLoop && !wakenUp.get()) {
                    long timeBlocked = System.nanoTime() - beforeSelect;
                    if (timeBlocked < minSelectTimeout) {

                        boolean notConnected = false;
                        // loop over all keys as the selector may was unblocked because of a closed channel
                        for (SelectionKey key: selector.keys()) {
                            SelectableChannel ch = key.channel();
                            try {
                                if (ch instanceof DatagramChannel && !ch.isOpen() ||
                                        ch instanceof SocketChannel && !((SocketChannel) ch).isConnected() &&
                                                // Only cancel if the connection is not pending.  See https://github.com/netty/netty/issues/2931
                                                !((SocketChannel) ch).isConnectionPending()) {
                                    notConnected = true;
                                    // cancel the key just to be on the safe side
                                    key.cancel();
                                }
                            } catch (CancelledKeyException e) {
                                // ignore
                            }
                        }
                        if (notConnected) {
                            selectReturnsImmediately = 0;
                        } else {
                            if (Thread.interrupted() && !shutdown) {
                                // Thread was interrupted but NioSelector was not shutdown.
                                // As this is most likely a bug in the handler of the user or it's client
                                // library we will log it.
                                //
                                // See https://github.com/netty/netty/issues/2426
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Selector.select() returned prematurely because the I/O thread " +
                                            "has been interrupted. Use shutdown() to shut the NioSelector down.");
                                }
                                selectReturnsImmediately = 0;
                            } else {
                                // Returned before the minSelectTimeout elapsed with nothing selected.
                                // This may be because of a bug in JDK NIO Selector provider, so increment the counter
                                // which we will use later to see if it's really the bug in JDK.
                                selectReturnsImmediately ++;
                            }
                        }
                    } else {
                        selectReturnsImmediately = 0;
                    }
                } else {
                    selectReturnsImmediately = 0;
                }

                if (SelectorUtil.EPOLL_BUG_WORKAROUND) {
                    if (selectReturnsImmediately == 1024) {
                        // The selector returned immediately for 10 times in a row,
                        // so recreate one selector as it seems like we hit the
                        // famous epoll(..) jdk bug.
                        rebuildSelector();
                        selector = this.selector;
                        selectReturnsImmediately = 0;
                        wakenupFromLoop = false;
                        // try to select again
                        continue;
                    }
                } else {
                    // reset counter
                    selectReturnsImmediately = 0;
                }


                if (wakenUp.get()) {
                    wakenupFromLoop = true;
                    System.out.println(Thread.currentThread().getName() + " for(;;) - 调用" + selector + "的wakeup()");
                    selector.wakeup();
                } else {
                    wakenupFromLoop = false;
                }
                cancelledKeys = 0;

                /**
                 * 第三 队列处理
                 */
                System.out.println(Thread.currentThread().getName() + " processTaskQueue ");
                processTaskQueue();

                // processTaskQueue() can call rebuildSelector()
                selector = this.selector;

                if (shutdown) {
                    this.selector = null;

                    // process one time again
                    processTaskQueue();

                    for (SelectionKey k: selector.keys()) {
                        close(k);
                    }

                    try {
                        selector.close();
                    } catch (IOException e) {
                        logger.warn("Failed to close a selector.", e);
                    }
                    shutdownLatch.countDown();
                    break;
                } else {

                    /**
                     * 第四 业务处理
                     */
                    System.out.println(Thread.currentThread().getName() + " process ==== ");
                    process(selector);
                }
            } catch (Throwable t) {
                logger.warn("Unexpected exception in the selector loop.", t);

                // Prevent possible consecutive immediate failures that lead to
                // excessive CPU consumption.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
    }


    /**
     *
     */
    private void processTaskQueue() {
        for (;;) {
            final Runnable task = taskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
            try {
                cleanUpCancelledKeys();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    protected final void increaseCancelledKeys() {
        cancelledKeys ++;
    }

    protected final boolean cleanUpCancelledKeys() throws IOException {
        if (cancelledKeys >= CLEANUP_INTERVAL) {
            cancelledKeys = 0;
            selector.selectNow();
            return true;
        }
        return false;
    }


    protected final boolean isIoThread() {
        return Thread.currentThread() == thread;
    }

    /**
     *  ??
     */
    public void rebuildSelector() {
        if (!isIoThread()) {

            taskQueue.add(new Runnable() {
                public void run() {
                    rebuildSelector();
                }
            });

            return;
        }

        final Selector oldSelector = selector;
        final Selector newSelector;

        if (oldSelector == null) {
            return;
        }

        try {
            newSelector = SelectorUtil.open();
        } catch (Exception e) {
            logger.warn("Failed to create a new Selector.", e);
            return;
        }

        // Register all channels to the new Selector.
        int nChannels = 0;
        for (;;) {
            try {
                for (SelectionKey key: oldSelector.keys()) {
                    try {
                        if (key.channel().keyFor(newSelector) != null) {
                            continue;
                        }

                        int interestOps = key.interestOps();
                        key.cancel();
                        key.channel().register(newSelector, interestOps, key.attachment());
                        nChannels ++;
                    } catch (Exception e) {
                        logger.warn("Failed to re-register a Channel to the new Selector,", e);
                        close(key);
                    }
                }
            } catch (ConcurrentModificationException e) {
                // Probably due to concurrent modification of the key set.
                continue;
            }

            break;
        }

        selector = newSelector;

        try {
            // time to close the old selector as everything else is registered to the new one
            oldSelector.close();
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close the old Selector.", t);
            }
        }

        logger.info("Migrated " + nChannels + " channel(s) to the new Selector,");
    }

    public void shutdown() {
        if (isIoThread()) {
            throw new IllegalStateException("Must not be called from a I/O-Thread to prevent deadlocks!");
        }

        Selector selector = this.selector;
        shutdown = true;
        if (selector != null) {
            selector.wakeup();
        }
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while wait for resources to be released #" + id);
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param selector
     * @throws IOException
     */
    protected abstract void process(Selector selector) throws IOException;

    /**
     * 调用选择器的.select()
     *
     * Boss重写了该方法，而Worker没有重写
     *
     * @param selector
     * @return
     * @throws IOException
     */
    protected int select(Selector selector) throws IOException {
        System.out.println(Thread.currentThread().getName() + " Worker = " + this + " selector = " + selector);
        return SelectorUtil.select(selector);
    }

    protected abstract void close(SelectionKey k);

    /**
     *
     * @param id
     * @param determiner
     * @return
     */
    protected abstract ThreadRenamingRunnable newThreadRenamingRunnable(int id, ThreadNameDeterminer determiner);

    /**
     *
     * @param channel
     * @param future
     * @return
     */
    protected abstract Runnable createRegisterTask(Channel channel, ChannelFuture future);
}
