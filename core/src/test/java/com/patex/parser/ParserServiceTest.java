package com.patex.parser;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;

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
        when(fileParser.parseFile(FILE_NAME, is)).thenReturn(bookInfo);
        ParserService parserService = new ParserService(fileParser);
        BookInfo result = parserService.getBookInfo(FILE_NAME, is);
        Assert.assertEquals(result, bookInfo);
    }

    @Test
    public void verifyCorrectParser() {
        FileParser fileParser = mock(FileParser.class);
        when(fileParser.getExtension()).thenReturn(EXT);
        InputStream is = mock(InputStream.class);
        BookInfo bookInfo = new BookInfo();
        when(fileParser.parseFile(FILE_NAME, is)).thenReturn(bookInfo);

        FileParser otherParser = mock(FileParser.class);
        when(otherParser.getExtension()).thenReturn("other");

        ParserService parserService = new ParserService(fileParser);
        BookInfo result = parserService.getBookInfo(FILE_NAME, is);

        Assert.assertEquals(bookInfo, result);
        verifyZeroInteractions(otherParser);
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
        Assert.assertEquals(result, iterator);
    }
}
