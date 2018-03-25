package com.patex.shingle;

import com.patex.shingle.byteSet.ByteHashSet;

import java.util.Iterator;

/**
 *
 */
class LoadedShingler implements Shingler {

    private final ByteHashSet shingles;


    LoadedShingler(ByteHashSet shingles) {
        this.shingles = shingles;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return shingles.iterator();
    }


    @Override
    public int size() {
        return shingles.getSize();
    }

    @Override
    public int getByteArraySize() {
        return shingles.getByteArraySize();
    }

    public boolean contains(byte[] shingleHash) {
        return shingles.contains(shingleHash);
    }

}
