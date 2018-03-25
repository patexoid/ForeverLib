package com.patex.shingle.byteSet;

import java.util.Arrays;

public class NodeVar implements Node {
    private final int hashCode;
    private final byte[] b;

    public NodeVar(byte[] key) {
        b = key;
        this.hashCode = ByteHashSet.getHashCode(key, key.length);
    }

    public boolean isEqualsArray(byte[] key) {
        return Arrays.equals(key, b);
    }

    @Override
    public Node getNext() {
        return null;
    }

    @Override
    public byte[] toBytes() {
        return b;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeVar nodeVar = (NodeVar) o;
        return Arrays.equals(b, nodeVar.b);
    }
}
