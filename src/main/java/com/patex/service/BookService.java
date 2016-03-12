package com.patex.service;

import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.BookRepository;
import com.patex.parser.Fb2FileParser;
import com.patex.parser.FileParser;
import com.patex.parser.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by Alexey on 12.03.2016.
 */
@Service
public class BookService {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ParserService parserService;
    public Book saveBook(String fileName, InputStream inputStream) throws LibException {
            Book book = parserService.getBookInfo(fileName, inputStream);
            book= bookRepository.save(book);
            return book;
    }

    public Book getBook(long id){
        return bookRepository.findOne(id);
    }
}
