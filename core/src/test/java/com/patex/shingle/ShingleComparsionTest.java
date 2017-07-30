package com.patex.shingle;

import com.patex.utils.shingle.ShingleComparsion;
import com.patex.utils.shingle.Shingleable;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class ShingleComparsionTest {

    @Test
    public void testSame() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> sameContent = new ArrayList<>(content);
        List<List<String>> similarContntents = Collections.singletonList(sameContent);
        Set<List<String>> result = new ShingleComparsion().findSimilar(content, similarContntents, this::toShingleable);
        Assert.assertTrue(result.containsAll(similarContntents));
    }


    @Test
    public void testBegin() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> similarContent1 = new ArrayList<>(content);
        similarContent1.add(0, RandomStringUtils.random(5));
        List<List<String>> similarContntents = Collections.singletonList(similarContent1);
        Set<List<String>> result = new ShingleComparsion().findSimilar(content, similarContntents, this::toShingleable);
        Assert.assertTrue(result.containsAll(similarContntents));
    }

    @Test
    public void testEnd() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());

        List<String> similarContent2 = new ArrayList<>(content);
        similarContent2.add(RandomStringUtils.random(5));

        List<List<String>> similarContntents = Collections.singletonList(similarContent2);
        Set<List<String>> result = new ShingleComparsion().findSimilar(content, similarContntents, this::toShingleable);
        Assert.assertTrue(result.containsAll(similarContntents));
    }

    @Test
    public void testMiddle() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> similarContent3 = new ArrayList<>(content);
        similarContent3.add(content.size() / 2, RandomStringUtils.random(5));
        List<List<String>> similarContntents = Collections.singletonList(similarContent3);
        Set<List<String>> result = new ShingleComparsion().findSimilar(content, similarContntents, this::toShingleable);
        Assert.assertTrue(result.containsAll(similarContntents));
    }

    @Test
    public void testNonSimilar() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> other = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<List<String>> similarContntents = Collections.singletonList(other);
        Set<List<String>> result = new ShingleComparsion().findSimilar(content, similarContntents, this::toShingleable);
        Assert.assertTrue(result.isEmpty());
    }


    private Shingleable toShingleable(List<String> list) {
        Iterator<String> iterator = list.iterator();
        return new Shingleable() {
            @Override
            public int size() {
                return list.size() * 6;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public String next() {
                return iterator.next();
            }
        };
    }

}
