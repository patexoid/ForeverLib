package com.patex.utils.shingle;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Created by Alexey on 16.07.2017.
 */
public class ShingleMatcher<T, ID> {

    private final Cache<ID, Shingler> cache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterAccess(0, TimeUnit.HOURS).build();
    private final Function<T, Shingleable> mapFunc;
    private final Function<T, ID> idFunc;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public ShingleMatcher(Function<T, Shingleable> mapFunc, Function<T, ID> idFunc) {
        this.mapFunc = mapFunc;
        this.idFunc = idFunc;
    }

    public boolean isSimilar(T primary, T secondary) {
        Shingler primaryShingler = getShigler(primary);
        Shingler secondaryShingler = getShigler(secondary);
        return primaryShingler.isSimilar(secondaryShingler);
    }


    private Shingler getShigler(T t) {
        ID id = idFunc.apply(t);
        rwLock.readLock().lock();
        try {
            Shingler shingler = cache.getIfPresent(id);
            if (shingler == null) {
                rwLock.readLock().unlock();
                rwLock.writeLock().lock();
                try {
                    shingler = cache.getIfPresent(id);
                    if (shingler == null) {
                        shingler = new Shingler(mapFunc.apply(t));
                        cache.put(id, shingler);
                    }
                } finally {
                    rwLock.writeLock().unlock();
                    rwLock.readLock().lock();
                }
            }
            return shingler;
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
