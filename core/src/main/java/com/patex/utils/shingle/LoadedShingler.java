package com.patex.utils.shingle;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 *
 */
class LoadedShingler implements Shingler {

    private final Byte16HashSet shingles;
    private int size;


    LoadedShingler(Byte16HashSet shingles, int size) {
        this.shingles = shingles;
        this.size = size;
    }

    @Override
    public @Nonnull
    Iterator<byte[]> iterator() {
        return shingles.iterator();
    }


    @Override
    public int size() {
        return size;
    }

    public boolean contains(byte[] shingleHash) {
        return shingles.contains(shingleHash);
    }

}
