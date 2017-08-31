package com.patex.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockingExecutor<T, R> {

    private static Logger log = LoggerFactory.getLogger(BlockingExecutor.class);

    private final BlockingQueue<Tuple<T, CompletableFuture<R>>> queue;

    public BlockingExecutor(int threadCount, int queueSize, Function<T, R> task) {
        queue = new LinkedBlockingQueue<>(queueSize);

        AtomicInteger count = new AtomicInteger(0);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName("DuplicateHandler-" + count.getAndIncrement());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> log.error("We have a problem: " + e.getMessage(), e));
            return thread;
        };

        ExecutorService executor = Executors.newFixedThreadPool(threadCount, threadFactory);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        Tuple<T, CompletableFuture<R>> tuple = queue.take();
                        CompletableFuture<R> cFuture = tuple._2;
                        T t = tuple._1;
                        try {
                            cFuture.complete(task.apply(t));
                        } catch (Exception e) {
                            cFuture.completeExceptionally(e);
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
    }

    public CompletableFuture<R> submit(T t) {
        try {
            CompletableFuture<R> future = new CompletableFuture<>();
            queue.put(new Tuple<>(t, future));
            return future;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<CompletableFuture<R>> submitAll(Collection<T> objs) {
        return objs.stream().map(this::submit).collect(Collectors.toList());
    }

}
