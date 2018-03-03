package com.patex.utils;

import org.slf4j.Logger;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorCreator {

    public static ExecutorService createExecutor(final String threadNamePrefix, Logger log) {
        return new DelegatingSecurityContextExecutorService(
                Executors.newCachedThreadPool(r -> {
                    AtomicInteger count = new AtomicInteger();
                    Thread thread = new Thread(r);
                    thread.setName(threadNamePrefix + "-" + count.incrementAndGet());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t, e) -> {
                        log.error(e.getMessage(), e);
                    });
                    return thread;
                }));
    }
}
