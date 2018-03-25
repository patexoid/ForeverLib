package com.patex.shingle.byteSet;

public class Node8 implements Node {
    private final int hashCode;
    private final byte b0, b1, b2, b3, b4, b5, b6, b7;

    public Node8(byte[] key) {
        b0 = key[0];
        b1 = key[1];
        b2 = key[2];
        b3 = key[3];
        b4 = key[4];
        b5 = key[5];
        b6 = key[6];
        b7 = key[7];
        this.hashCode = ByteHashSet.getHashCode(key, 8);
    }

    public boolean isEqualsArray(byte[] key) {

        if (b0 != key[0]) return false;
        if (b1 != key[1]) return false;
        if (b2 != key[2]) return false;
        if (b3 != key[3]) return false;
        if (b4 != key[4]) return false;
        if (b5 != key[5]) return false;
        if (b6 != key[6]) return false;
        if (b7 != key[7]) return false;
        return true;
    }

    @Override
    public Node getNext() {
        return null;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[8];
        bytes[0] = b0;
        bytes[1] = b1;
        bytes[2] = b2;
        bytes[3] = b3;
        bytes[4] = b4;
        bytes[5] = b5;
        bytes[6] = b6;
        bytes[7] = b7;
        return bytes;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node8 node8 = (Node8) o;
        return  b0 == node8.b0 &&
                b1 == node8.b1 &&
                b2 == node8.b2 &&
                b3 == node8.b3 &&
                b4 == node8.b4 &&
                b5 == node8.b5 &&
                b6 == node8.b6 &&
                b7 == node8.b7;
    }
}
