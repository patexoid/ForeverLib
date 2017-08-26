package com.patex.utils.shingle;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
        checkSimilarity(content, sameContent);
    }


    @Test
    public void testBegin() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> similarContents = new ArrayList<>(content);
        similarContents.add(0, RandomStringUtils.random(5));
        checkSimilarity(content, similarContents);
    }

    private void checkSimilarity(List<String> content, List<String> similarContents) {
        ShingleMatcher<List<String>, List<String>> shingleMatcher = new ShingleMatcher<>(this::toShingleable, o -> o);
        Assert.assertTrue(shingleMatcher.isSimilar(content, similarContents));
    }

    @Test
    public void testEnd() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());

        List<String> similarContent = new ArrayList<>(content);
        similarContent.add(RandomStringUtils.random(5));
        checkSimilarity(content, similarContent);
    }

    @Test
    public void testMiddle() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> similarContent = new ArrayList<>(content);
        similarContent.add(content.size() / 2, RandomStringUtils.random(5));

        checkSimilarity(content, similarContent);
    }

    @Test
    public void testNonSimilar() {
        Random random = new Random();
        List<String> content = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        List<String> other = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(100).collect(Collectors.toList());
        ShingleMatcher<List<String>, List<String>> shingleMatcher = new ShingleMatcher<>(this::toShingleable, o -> o);
        Assert.assertFalse(shingleMatcher.isSimilar(content, other));
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
