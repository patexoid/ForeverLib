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

    private final Cache<ID, Shingler> cache =
            CacheBuilder.newBuilder().
                    maximumSize(100).
                    softValues().
                    expireAfterAccess(10, TimeUnit.MINUTES).build();
    private final Function<T, Shingleable> mapFunc;
    private final Function<T, ID> idFunc;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public ShingleMatcher(Function<T, Shingleable> mapFunc, Function<T, ID> idFunc) {
        this.mapFunc = mapFunc;
        this.idFunc = idFunc;
    }

    public boolean isSimilar(T first, T second) {
        Shingler firstS = getShigler(first);
        Shingler secondS = getShigler(second);
        boolean similar = isSimilar(firstS, secondS);

        checkIsLoaded(first);
        checkIsLoaded(second);
        return similar;
    }

    private void checkIsLoaded(T t) {
        Shingler shigler = getShigler(t);
        if (shigler instanceof LazyShingler && !((LazyShingler) shigler).isLoading()) {
            cache.put(idFunc.apply(t),
                    new LoadedShingler(((LazyShingler) shigler).getShingles(), shigler.size()));
        }
    }

    private boolean isSimilar(Shingler first, Shingler second) {
        Shingler bigger, smaller;
        if (first.size() > second.size()) {
            bigger = first;
            smaller = second;
        } else {
            smaller = first;
            bigger = second;
        }
        int notmatch = smaller.size() / 5;
        for (byte[] shingleHash : smaller) {
            if (!bigger.contains(shingleHash)) {
                notmatch--;
                if (notmatch < 0) {
                    return false;
                }
            }
        }
        return true;
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
                        shingler = new LazyShingler(mapFunc.apply(t));
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

    public void invalidate(T obj) {
        cache.invalidate(idFunc.apply(obj));
    }
}
