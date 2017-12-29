package com.patex.shingle;

public interface Shingler extends Iterable<byte[]>{

    int size();

    boolean contains(byte[] shingleHash);

    default int byteSize(){
        return 16;
    }
}
