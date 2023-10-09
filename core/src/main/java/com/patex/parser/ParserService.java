package com.patex.parser;

import com.patex.zombie.LibException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */

@Service
public class ParserService {

    private final Map<String, FileParser> parserMap = new HashMap<>();

    @Autowired
    public ParserService(FileParser... parsers) {
        for (FileParser parser : parsers) {
            parserMap.put(parser.getExtension(), parser);
        }
    }

    public BookInfo getBookInfo(String fileName, InputStream stream, boolean parseBody) throws LibException {
        FileParser parser = getParser(fileName);
        return parser.parseFile(fileName, stream, parseBody);
    }

    private FileParser getParser(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        FileParser parser = parserMap.get(extension);
        if (parser == null) {
            throw new LibException("unsupportd extension: " + extension);
        }
        return parser;
    }

    public Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException {
        FileParser parser = getParser(fileName);
        return parser.getContentIterator(fileName, is);
    }
}
