package com.patex.parser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.*;

public class ZipFileParserTest {


    private static final String FILENAME = "test.dummy";
    private static final String FILENAME_ZIP = FILENAME + ".zip";

    @Test
    public void verifyParseFile() throws Exception {
        ParserService parserService = mock(ParserService.class);
        ZipFileParser parser = new ZipFileParser(parserService);
        byte[] data = {1, 2, 3, 4, 5};
        ByteArrayOutputStream baos = new ByteArrayOutputStream(5);
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry("blah"));
        zos.write(data);
        parser.parseFile(FILENAME + ".zip", new ByteArrayInputStream(data));
        verify(parserService).getBookInfo(eq(FILENAME), any());
    }

    @Test
    public void verifyContentIterator() throws Exception {
        ParserService parserService = mock(ParserService.class);
        CloseableIterator filenameIterator = mock(CloseableIterator.class);
        when(parserService.getContentIterator(eq(FILENAME), any())).thenReturn(filenameIterator);
        ZipFileParser parser = new ZipFileParser(parserService);
        byte[] data = {1, 2, 3, 4, 5};
        ByteArrayOutputStream baos = new ByteArrayOutputStream(5);
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry("blah"));
        zos.write(data);
        CloseableIterator zipIterator = parser.getContentIterator(FILENAME_ZIP, new ByteArrayInputStream(data));

        verifyZeroInteractions(filenameIterator);
        zipIterator.next();
        verify(filenameIterator).next();
        verifyNoMoreInteractions(filenameIterator);
        zipIterator.hasNext();
        verify(filenameIterator).hasNext();
        verifyNoMoreInteractions(filenameIterator);
        zipIterator.close();
        verify(filenameIterator).close();
        verifyNoMoreInteractions(filenameIterator);

    }
}
