package com.patex.parser;

import com.patex.entities.Book;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.junit.Assert.*;

public class Fb2ParserTest {

    @Test
    public void testFb2Parser() throws Exception {
        Fb2FileParser fb2FileParser = new Fb2FileParser();
        InputStream resourceAsStream = getClass().getResourceAsStream("/parserTest.fb2");
        Book book = fb2FileParser.parseFile("parserTest.fb2", resourceAsStream);
        assertEquals(book.getAuthorBooks().get(0).getAuthor().getName(),"Третьевенко Первый Вторович");
        assertEquals(book.getSequences().get(0).getSeqOrder(),1);
        assertEquals(book.getSequences().get(0).getSequence().getName(),"Мегасерия");
        assertThat(book.getGenres(), hasSize(1));
        assertEquals(book.getGenres().get(0).getGenre().getName(), "sf");
        assertEquals(book.getTitle(),"Первая книга из Мегасерии");
        assertNotNull(book.getDescr());
        assertTrue(book.getDescr().contains("Подробное описание книги"));

    }
}
