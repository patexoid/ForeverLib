package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Function;
import java.util.zip.ZipInputStream;

/**
 * 5.
 */
@Service
public class ZipFileParser implements FileParser {

    @Autowired()
    @Lazy
    private ParserService parserService;

    @Override
    public String getExtension() {
        return "zip";
    }

    @Override
    public synchronized Book parseFile(String fileName, InputStream is) throws LibException {
        return goDeep(is, zis -> parserService.getBookInfo(fileName.substring(0, fileName.lastIndexOf('.')), zis));
    }

    private <T> T goDeep(InputStream is, Function<InputStream, T> f) throws LibException {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            zis.getNextEntry();
            return f.apply(zis);
        } catch (IOException e) {
            throw new LibException(e);
        }
    }

    public Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException {
        final ZipInputStream zis = new ZipInputStream(is);
        try {
            zis.getNextEntry();
        } catch (IOException e) {
            throw new LibException(e.getMessage(), e);
        }
        Iterator<String> iterator = parserService.getContentIterator(fileName.substring(0, fileName.lastIndexOf('.')), zis);
        return new CloseableIterator() {
            @Override
            public void close() {
                try {
                    zis.close();
                    if(iterator instanceof Closeable){
                        ((Closeable) iterator).close();
                    }
                } catch (IOException e) {
                    throw new LibException(e.getMessage(), e);
                }
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
