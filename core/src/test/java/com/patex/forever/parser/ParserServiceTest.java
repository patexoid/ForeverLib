package com.patex.forever.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ParserServiceTest {


    private static final String EXT = "parser";
    private static final String FILE_NAME = "filename." + EXT;

    @Test
    public void verifyParser() {
        FileParser fileParser = mock(FileParser.class);
        when(fileParser.getExtension()).thenReturn(EXT);
        InputStream is = mock(InputStream.class);
        BookInfo bookInfo = new BookInfo();
        when(fileParser.parseFile(FILE_NAME, is, true)).thenReturn(bookInfo);
        ParserService parserService = new ParserService(fileParser);
        BookInfo result = parserService.getBookInfo(FILE_NAME, is, true);
        assertEquals(result, bookInfo);
    }

    @Test
    public void verifyCorrectParser() {
        FileParser fileParser = mock(FileParser.class);
        when(fileParser.getExtension()).thenReturn(EXT);
        InputStream is = mock(InputStream.class);
        BookInfo bookInfo = new BookInfo();
        when(fileParser.parseFile(FILE_NAME, is, true)).thenReturn(bookInfo);

        FileParser otherParser = mock(FileParser.class);
        when(otherParser.getExtension()).thenReturn("other");

        ParserService parserService = new ParserService(fileParser);
        BookInfo result = parserService.getBookInfo(FILE_NAME, is, true);

        assertEquals(bookInfo, result);
        verifyNoInteractions(otherParser);
    }

    @Test
    public void verifyContentIterator() {
        FileParser fileParser = mock(FileParser.class);
        when(fileParser.getExtension()).thenReturn(EXT);
        InputStream is = mock(InputStream.class);
        Iterator<String> iterator = mock(Iterator.class);
        when(fileParser.getContentIterator(FILE_NAME, is)).thenReturn(iterator);
        ParserService parserService = new ParserService(fileParser);
        Iterator<String> result= parserService.getContentIterator(FILE_NAME, is);
        assertEquals(result, iterator);
    }
}
