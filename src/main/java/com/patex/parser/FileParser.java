package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Book;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by Alexey on 12.03.2016.
 */
public interface FileParser {

    public String getExtension();
    Book parseFile(String fileName, InputStream file) throws LibException;
}
