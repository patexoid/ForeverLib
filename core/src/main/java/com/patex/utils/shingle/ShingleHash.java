package com.patex.utils.shingle;

import java.util.Arrays;

/**
 * Created by Alexey on 16.07.2017.
 */
class ShingleHash {
    private final byte[] hash;
    private final int hashCode;

    ShingleHash(byte[] hash) {
        this.hash = hash;
        hashCode = Arrays.hashCode(hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShingleHash that = (ShingleHash) o;
        return Arrays.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
