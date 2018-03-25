package com.patex.shingle.byteSet;

public class Node16 implements Node {
    private final int hashCode;
    private final byte b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15;

    public Node16(byte[] key) {
        b0 = key[0];
        b1 = key[1];
        b2 = key[2];
        b3 = key[3];
        b4 = key[4];
        b5 = key[5];
        b6 = key[6];
        b7 = key[7];
        b8 = key[8];
        b9 = key[9];
        b10 = key[10];
        b11 = key[11];
        b12 = key[12];
        b13 = key[13];
        b14 = key[14];
        b15 = key[15];

        this.hashCode = ByteHashSet.getHashCode(key,16);
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
        if (b8 != key[8]) return false;
        if (b9 != key[9]) return false;
        if (b10 != key[10]) return false;
        if (b11 != key[11]) return false;
        if (b12 != key[12]) return false;
        if (b13 != key[13]) return false;
        if (b14 != key[14]) return false;
        //noinspection RedundantIfStatement
        if (b15 != key[15]) return false;

        return true;
    }

    @Override
    public Node getNext() {
        return null;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[16];
        bytes[0] = b0;
        bytes[1] = b1;
        bytes[2] = b2;
        bytes[3] = b3;
        bytes[4] = b4;
        bytes[5] = b5;
        bytes[6] = b6;
        bytes[7] = b7;
        bytes[8] = b8;
        bytes[9] = b9;
        bytes[10] = b10;
        bytes[11] = b11;
        bytes[12] = b12;
        bytes[13] = b13;
        bytes[14] = b14;
        bytes[15] = b15;
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
        Node16 node16 = (Node16) o;
        return  b0 == node16.b0 &&
                b1 == node16.b1 &&
                b2 == node16.b2 &&
                b3 == node16.b3 &&
                b4 == node16.b4 &&
                b5 == node16.b5 &&
                b6 == node16.b6 &&
                b7 == node16.b7 &&
                b8 == node16.b8 &&
                b9 == node16.b9 &&
                b10 == node16.b10 &&
                b11 == node16.b11 &&
                b12 == node16.b12 &&
                b13 == node16.b13 &&
                b14 == node16.b14 &&
                b15 == node16.b15;
    }
}
