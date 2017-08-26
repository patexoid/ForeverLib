package com.patex.utils.shingle;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

public class Byte16HashSetTest {

    @Test
    public void testExists() throws Exception {
        Byte16HashSet set = new Byte16HashSet(64);
        byte[] key = RandomUtils.nextBytes(16);
        set.add(key);
        Assert.assertTrue(set.contains(key));
    }

    @Test
    public void testNotExists() throws Exception {
        Byte16HashSet set = new Byte16HashSet(64);
        byte[] key = RandomUtils.nextBytes(16);
        set.add(key);
        key[0] += 1;
        Assert.assertFalse(set.contains(key));
    }


    @Test
    public void testExistsSameHash() throws Exception {
        Byte16HashSet set = new Byte16HashSet(64);
        //                         0   1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
        byte[] key1 = new byte[]{-30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] key2 = new byte[]{-31, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Assert.assertTrue("hashCode calculation was changed",
                Byte16HashSet.getHashCode(key1) == Byte16HashSet.getHashCode(key2));
        set.add(key1);
        Assert.assertTrue(set.contains(key1));
        Assert.assertFalse(set.contains(key2));
        set.add(key2);
        Assert.assertTrue(set.contains(key1));
        Assert.assertTrue(set.contains(key2));
    }
}
