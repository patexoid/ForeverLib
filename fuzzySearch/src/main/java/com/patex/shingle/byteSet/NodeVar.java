package com.patex.shingle.byteSet;

import java.util.Arrays;

public class NodeVar implements Node {
    final int hashCode;
    final byte[] b;

    public NodeVar(byte[] key) {
        b=key;
        this.hashCode = ByteHashSet.getHashCode(key);
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
}
