package com.patex.utils;

import org.slf4j.Logger;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExecutorCreator {

    public ExecutorService createExecutor(final String threadNamePrefix, Logger log) {
        return new DelegatingSecurityContextExecutorService(
                Executors.newCachedThreadPool(createThreadFactory(threadNamePrefix, log)));
    }

    public ThreadFactory createThreadFactory(String threadNamePrefix, Logger log) {
        AtomicInteger count = new AtomicInteger();
        return r -> {
            Thread thread = new Thread(r);
            thread.setName(threadNamePrefix + "-" + count.incrementAndGet());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> {
                log.error(e.getMessage(), e);
            });
            return thread;
        };
    }
}
