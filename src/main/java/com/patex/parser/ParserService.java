package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Book;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexey on 12.03.2016.
 */

@Service
public class ParserService {

    private Map<String, FileParser> parserMap=new HashMap<>();

    public Book getBookInfo(String fileName, InputStream stream) throws LibException{
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        FileParser parser = parserMap.get(extension);
        if(parser==null){
            throw new LibException("unsupportd extension: "+extension);
        }
        return parser.parseFile(fileName, stream);

    }

    public void registerParser(FileParser fileParser){
        parserMap.put(fileParser.getExtension(),fileParser);
    }
}
