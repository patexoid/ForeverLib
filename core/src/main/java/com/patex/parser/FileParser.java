package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Book;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by Alexey on 12.03.2016.
 */
public interface FileParser {

    String getExtension();

    Book parseFile(String fileName, InputStream is) throws LibException;

    Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException;
}
