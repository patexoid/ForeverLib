package com.patex.shingle;


import java.io.InputStream;

public interface ShingleCacheStorage<T> {

    InputStream load(T t);

    void save(byte[] bytes, T t);


}
