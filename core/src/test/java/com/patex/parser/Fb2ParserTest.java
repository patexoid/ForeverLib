package com.patex.parser;

import com.patex.entities.Book;
import fb2Generator.Fb2Creator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Stream;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

public class Fb2ParserTest {

    @Test
    public void testFb2BookParser() throws Exception {
        Fb2FileParser fb2FileParser = new Fb2FileParser();
        InputStream resourceAsStream = getClass().getResourceAsStream("/parserTest.fb2");
        Book book = fb2FileParser.parseFile("parserTest.fb2", resourceAsStream);
        assertEquals(book.getAuthorBooks().get(0).getAuthor().getName(), "Третьевенко Первый Вторович");
        assertEquals(book.getSequences().get(0).getSeqOrder(), 1);
        assertEquals(book.getSequences().get(0).getSequence().getName(), "Мегасерия");
        assertThat(book.getGenres(), hasSize(1));
        assertEquals(book.getGenres().get(0).getGenre().getName(), "sf");
        assertEquals(book.getTitle(), "Первая книга из Мегасерии");
        assertNotNull(book.getDescr());
        assertTrue(book.getDescr().contains("Подробное описание книги"));

    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testFb2Content() throws Exception {
        ParserService parserService = new ParserService(new Fb2FileParser());

        Random random = new Random();
        String content1 = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(5).reduce((s, s2) -> s + " " + s2).get();
        String content2 = Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                limit(5).reduce((s, s2) -> s + " " + s2).get();

        Fb2Creator book = new Fb2Creator("book");
        book.addContent(content1);
        book.addContent(content2);

        Iterator<String> contentIterator = parserService.getContentIterator("book.fb2", book.getFbook());
        assertEquals(content1, contentIterator.next().trim());
        assertEquals(content2, contentIterator.next().trim());
        assertFalse(contentIterator.hasNext());
    }
}
