package com.patex.shingle.byteSet;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

public class ByteHashSetTest {

    @Test
    public void testExists() {
        ByteHashSet set = ByteSetFactory.createByteSet(64, 16);
        byte[] key = RandomUtils.nextBytes(16);
        set.add(key);
        Assert.assertTrue(set.contains(key));
    }

    @Test
    public void testNotExists() {
        ByteHashSet set = ByteSetFactory.createByteSet(64, 16);
        byte[] key = RandomUtils.nextBytes(16);
        set.add(key);
        key[0] += 1;
        Assert.assertFalse(set.contains(key));
    }


    @Test
    public void testExistsSameHash() {
        ByteHashSet set = ByteSetFactory.createByteSet(64, 16);
        //                         0   1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
        byte[] key1 = new byte[]{-30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] key2 = new byte[]{-31, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Assert.assertTrue("hashCode calculation was changed",
        ByteHashSet.getHashCode(key1) == ByteHashSet.getHashCode(key2));
        set.add(key1);
        Assert.assertTrue(set.contains(key1));
        Assert.assertFalse(set.contains(key2));
        set.add(key2);
        Assert.assertTrue(set.contains(key1));
        Assert.assertTrue(set.contains(key2));
    }
}
