package com.patex.forever.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockingExecutor {

   private static final Logger log = LoggerFactory.getLogger(BlockingExecutor.class);

    private final BlockingQueue<Runnable> queue;
    private final ThreadPoolExecutor executor;


    public BlockingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueSize, ThreadFactory threadFactory) {
        queue = new LinkedBlockingQueue<>(queueSize);
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueSize), threadFactory){
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                queue.poll();
                super.beforeExecute(t, r);
            }
        };
    }

    public void execute(Runnable command) {
        try {
            queue.put(command);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        executor.execute(command);
    }

}
