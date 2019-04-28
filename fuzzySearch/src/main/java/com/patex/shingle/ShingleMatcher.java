package com.patex.shingle;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.patex.shingle.byteSet.ByteHashSet;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Alexey on 16.07.2017.
 */
public class ShingleMatcher<T, ID> {

    private final Cache<ID, Shingler> cache;
    private final Function<T, Shingleable> mapFunc;
    private final Function<T, ID> idFunc;
    private final ShingleCache<T> shingleCache;
    private final LoadedShinglerFactory shinglerCreator;

    public ShingleMatcher(Function<T, Shingleable> mapFunc, Function<T, ID> idFunc, int coef, int cacheSize,
                          int byteArraySize) {
        this.mapFunc = mapFunc;
        this.idFunc = idFunc;
        shingleCache = new ShingleCache<>();
        cache = CacheBuilder.newBuilder().
                maximumSize(cacheSize).
                expireAfterAccess(10, TimeUnit.MINUTES).build();
        shinglerCreator = new LoadedShinglerFactory(coef, byteArraySize,
                () -> {
                    ThreadLocal<MessageDigest> digestTL = ThreadLocal.withInitial(() -> {
                        try {
                            return MessageDigest.getInstance("MD5");
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return bytes -> {
                        MessageDigest digest = digestTL.get();
                        byte[] result = digest.digest(bytes);
                        digest.reset();
                        return result;
                    };
                });
    }

    public ShingleMatcher(Function<T, Shingleable> mapFunc, Function<T, ID> idFunc, int coef, int cacheSize) {
        this(mapFunc, idFunc, coef, cacheSize, 16);
    }

    public boolean isSimilar(T first, T second) {
        Shingler firstS = getShingler(first);
        Shingler secondS = getShingler(second);
        return isSimilar(firstS, secondS);
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
        if (((float) smaller.size()) / ((float) bigger.size()) < 0.7f) {
            return false;
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

    private Shingler getShingler(T t) {
        ID id = idFunc.apply(t);
        try {
            return cache.get(id, () ->
                    shingleCache.getFromCache(t).orElseGet(() ->
                            createShingler(t)));
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Shingler createShingler(T t) {
        try {
            ByteHashSet shingleSet = shinglerCreator.createShingles(mapFunc.apply(t));
            LoadedShingler shingler = new LoadedShingler(shingleSet);
            shingleCache.saveToCache(shingler, t);
            return shingler;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void invalidate(T obj) {
        cache.invalidate(idFunc.apply(obj));
    }

    public void setStorage(ShingleCacheStorage<T> storage) {
        shingleCache.setStorage(storage);
    }
}
