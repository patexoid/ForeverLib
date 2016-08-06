package com.patex.parser;

import com.patex.entities.Book;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class Fb2ParserTest {

    @Test
    public void testFb2Parser() throws Exception {
        Fb2FileParser fb2FileParser = new Fb2FileParser();
        InputStream resourceAsStream = getClass().getResourceAsStream("/parserTest.fb2");
        Book book = fb2FileParser.parseFile("parserTest.fb2", resourceAsStream);
        assertEquals(book.getAuthors().get(0).getName(),"Фамилия First Second");
        assertEquals(book.getSequences().get(0).getSeqOrder(),1);
        assertEquals(book.getSequences().get(0).getSequence().getName(),"Серия");
        assertThat(book.getGenres(), hasSize(1));
        assertEquals(book.getGenres().get(0).getGenre().getName(), "sf");
        assertEquals(book.getTitle(),"Заголовок");

    }
}
