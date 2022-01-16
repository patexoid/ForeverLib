package com.patex.messaging;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextSpliteratorTest {

    @Test
    public void shouldNotSplit() {
        String message = "111";

        TextSpliterator spliterator = new TextSpliterator(6, Arrays.asList("\n", "."));
        List<String> strings = spliterator.splitText(message);

        assertThat(strings, hasSize(1));
        assertEquals(message, strings.get(0));
    }

    @Test
    public void shouldSplit1() {
        String message = "1111\n2222";

        TextSpliterator spliterator = new TextSpliterator(6, Arrays.asList("\n", "."));
        List<String> strings = spliterator.splitText(message);

        assertThat(strings, hasSize(2));
        assertEquals("1111", strings.get(0));
        assertEquals("2222", strings.get(1));
    }

    @Test
    public void shouldSplit2() {
        String message = "1111.2222";

        TextSpliterator spliterator = new TextSpliterator(6, Arrays.asList("\n", "."));
        List<String> strings = spliterator.splitText(message);

        assertThat(strings, hasSize(2));
        assertEquals("1111.", strings.get(0));
        assertEquals("2222", strings.get(1));
    }

    @Test
    public void shouldSplit3() {
        String message = "1111\n22\n22";

        TextSpliterator spliterator = new TextSpliterator(6, Arrays.asList("\n", "."));
        List<String> strings = spliterator.splitText(message);

        assertThat(strings, hasSize(2));
        assertEquals("1111", strings.get(0));
        assertEquals("22\n22", strings.get(1));
    }

    @Test
    public void shouldSplit4() {
        String message = "4444444";

        TextSpliterator spliterator = new TextSpliterator(6, Arrays.asList("\n", "."));
        List<String> strings = spliterator.splitText(message);

        assertThat(strings, hasSize(2));
        assertEquals("444", strings.get(0));
        assertEquals("4444", strings.get(1));
    }

    @Test
    public void shouldSplit() {
        String message = "11115\n2222.3333.4444444";

        TextSpliterator spliterator = new TextSpliterator(6, Arrays.asList("\n", "."));
        List<String> strings = spliterator.splitText(message);

        System.out.println(strings);
        assertThat(strings, hasSize(5));
        assertEquals("11115", strings.get(0));
        assertEquals("2222", strings.get(1));
        assertEquals(".3333", strings.get(2));
        assertEquals(".444", strings.get(3));
        assertEquals("4444", strings.get(4));
    }
}
