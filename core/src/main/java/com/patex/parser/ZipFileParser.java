package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Created by alex on 15.03.2015.
 */
@Service
public class ZipFileParser implements FileParser {

    @Autowired
    ParserService parserService;

    @PostConstruct
    public void register() {
        parserService.registerParser(this);
    }

    @Override
    public String getExtension() {
        return "zip";
    }

    @Override
    public synchronized Book parseFile(String fileName, InputStream file) throws LibException {
        try (ZipInputStream zis = new ZipInputStream(file)) {
            zis.getNextEntry();
            return parserService.getBookInfo(fileName.substring(0, fileName.lastIndexOf('.')), zis);
        } catch (IOException e) {
            throw new LibException(e);
        }
    }
}
