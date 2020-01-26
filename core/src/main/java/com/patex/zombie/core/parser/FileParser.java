package com.patex.zombie.core.parser;

import com.patex.LibException;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by Alexey on 12.03.2016.
 */
public interface FileParser {

    String getExtension();

    BookInfo parseFile(String fileName, InputStream is) throws LibException;

    Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException;
}
