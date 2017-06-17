package com.patex.extlib;

/**
 * Created by Alexey on 16.06.2017.
 */
@FunctionalInterface
public interface ExtLibFunction<T, R> {
    R apply(T t) throws Exception;
}
