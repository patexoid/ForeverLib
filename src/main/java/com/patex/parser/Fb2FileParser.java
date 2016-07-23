package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.Book;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

@Service
public class Fb2FileParser implements FileParser {


    private final XMLInputFactory factory;

    public Fb2FileParser() {
        factory = XMLInputFactory.newInstance();
    }

    @Autowired
    ParserService parserService;

    @PostConstruct
    public void register() {
        parserService.registerParser(this);
    }

    @Override
    public String getExtension() {
        return "fb2";
    }


    @Override
    public Book parseFile(String fileName, InputStream file) throws LibException {
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(file);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && "title-info".equals(reader.getLocalName())) {
                    return parseTitleInfo(reader);
                }
            }
        } catch (XMLStreamException e) {
            throw new LibException(e.getMessage(), e);
        }
        throw new LibException("unable to parse fb2 file");
    }

    private Book parseTitleInfo(XMLStreamReader reader) throws XMLStreamException {
        Book book = new Book();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT && "title-info".equals(reader.getLocalName())) {
                return book;
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                if ("author".equals(reader.getLocalName())) {
                    Author author = parseAuthor(reader);
                    book.addAuthor(author);
                } else if ("book-title".equals(reader.getLocalName())) {
                    book.setTitle(reader.getElementText());
                 } else if ("sequence".equals(reader.getLocalName())) {
                    Integer order;
                    try {
                        order = Integer.valueOf(reader.getAttributeValue("", "number"));
                    } catch (NumberFormatException e) {
                        order=0;
                        e.printStackTrace();
                    }
                    book.getSequences().add(new BookSequence(order,new Sequence(reader.getAttributeValue("","name"))));
                }

            }
        }
        return null;
    }

    private Author parseAuthor(XMLStreamReader reader) throws XMLStreamException {
        String lastName = "";
        String firstName = "";
        String middleName = "";
        Author author = new Author();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("first-name".equals(reader.getLocalName())) {
                    firstName = reader.getElementText();
                } else if ("middle-name".equals(reader.getLocalName())) {
                    middleName = reader.getElementText();
                } else if ("last-name".equals(reader.getLocalName())) {
                    lastName = reader.getElementText();
                }
            }
            if (event == XMLStreamConstants.END_ELEMENT && "author".equals(reader.getLocalName())) {
                author.setName(lastName + " " + firstName + " " + middleName);
                return author;
            }
        }
        return null;
    }
}
