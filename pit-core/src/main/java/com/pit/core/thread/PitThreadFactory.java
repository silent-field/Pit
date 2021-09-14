package com.pit.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gy
 * @date 2020/3/23
 */
public class PitThreadFactory implements ThreadFactory {
    protected String threadName;
    protected AtomicInteger nextId = new AtomicInteger();

    public PitThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, this.threadName + '-' + this.nextId.getAndIncrement());
        return thread;
    }
}