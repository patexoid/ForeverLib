package com.patex.shingle.byteSet;

public interface Node {

    Node getNext();

    byte[] toBytes();

    boolean isEqualsArray(byte[] key);
}
