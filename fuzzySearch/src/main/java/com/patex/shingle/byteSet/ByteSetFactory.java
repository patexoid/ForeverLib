package com.patex.shingle.byteSet;

public class ByteSetFactory {

    public static ByteHashSet createByteSet(int size, int byteArraySize) {
        if (byteArraySize == 8) {
            return new ByteHashSet(size, byteArraySize, Node8::new, Node8Next::new);
        } else if (byteArraySize == 16) {
            return new ByteHashSet(size, byteArraySize, Node16::new, Node16Next::new);
        }
        return new ByteHashSet(size, byteArraySize, NodeVar::new, NodeVarNext::new);
    }
}
