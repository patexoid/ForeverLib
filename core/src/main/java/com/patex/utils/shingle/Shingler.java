package com.patex.utils.shingle;

public interface Shingler extends Iterable<byte[]>{

    int size();

    boolean contains(byte[] shingleHash);
}
