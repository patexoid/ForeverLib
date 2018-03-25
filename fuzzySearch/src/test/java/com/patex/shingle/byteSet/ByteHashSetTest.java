package com.patex.shingle.byteSet;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class ByteHashSetTest {

    @Parameterized.Parameter
    public Integer byteArraySize;

    @Parameterized.Parameters
    public static Iterable<Integer> data() {
        return Arrays.asList(8, 16, 24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSize(){
        ByteSetFactory.createByteSet(-64, byteArraySize);
    }

    @Test
    public void testExists() {
        ByteHashSet set = ByteSetFactory.createByteSet(64, byteArraySize);
        byte[] key = RandomUtils.nextBytes(byteArraySize);
        set.add(key);
        Assert.assertTrue(set.contains(key));
    }

    @Test
    public void testNotExists() {
        ByteHashSet set = ByteSetFactory.createByteSet(64, byteArraySize);
        byte[] key = RandomUtils.nextBytes(byteArraySize);
        set.add(key);
        key[0] += 1;
        Assert.assertFalse(set.contains(key));
    }


    @Test
    public void testExistsSameHash() {
        ByteHashSet set = ByteSetFactory.createByteSet(64, byteArraySize);
        //                         0   1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
        byte[] key1 = new byte[byteArraySize];
        byte[] key2 = new byte[byteArraySize];
        Arrays.fill(key1, (byte) 0);
        Arrays.fill(key2, (byte) 0);
        key1[0] = -30;
        key2[0] = -31;
        key2[1] = 31;
        Assert.assertTrue("hashCode calculation was changed",
                ByteHashSet.getHashCode(key1, byteArraySize) == ByteHashSet.getHashCode(key2, byteArraySize));
        set.add(key1);
        Assert.assertTrue(set.contains(key1));
        Assert.assertFalse(set.contains(key2));
        set.add(key2);
        Assert.assertTrue(set.contains(key1));
        Assert.assertTrue(set.contains(key2));
    }
}
