package com.patex.parser;

import com.patex.zombie.LibException;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by Alexey on 12.03.2016.
 */
public interface FileParser {

    String getExtension();

    BookInfo parseFile(String fileName, InputStream is, boolean parseBody);

    Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException;
}
