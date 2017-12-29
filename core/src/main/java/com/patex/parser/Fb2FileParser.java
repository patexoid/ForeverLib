package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.Book;
import com.patex.entities.BookGenre;
import com.patex.entities.BookSequence;
import com.patex.entities.Genre;
import com.patex.entities.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class Fb2FileParser implements FileParser {

    private static Logger log = LoggerFactory.getLogger(Fb2FileParser.class);

        private final XMLInputFactory factory;

    public Fb2FileParser() {
        factory = XMLInputFactory.newInstance();
    }

    @Override
    public String getExtension() {
        return "fb2";
    }


    @Override
    public Book parseFile(String fileName, InputStream is) throws LibException {
        try {
            XMLEventReader reader = factory.createXMLEventReader(is);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement() && "title-info".equals(event.asStartElement().getName().getLocalPart())) {
                    return parseTitleInfo(reader);
                }
            }
        } catch (XMLStreamException e) {
            throw new LibException(e.getMessage(), e);
        }
        throw new LibException("unable to parse fb2 file");
    }

    private Book parseTitleInfo(XMLEventReader reader) throws XMLStreamException {
        Book book = new Book();
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && "title-info".equals(event.asEndElement().getName().getLocalPart())) {
                return book;
            } else if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String localPart = element.getName().getLocalPart();
                if ("author".equals(localPart)) {
                    Author author = parseAuthor(reader);
                    book.addAuthor(author);
                } else if ("book-title".equals(localPart)) {
                    book.setTitle(reader.getElementText());
                } else if ("annotation".equals(localPart)) {
                    book.setDescr(getText(reader, "annotation"));
                } else if ("genre".equals(localPart)) {
                    book.getGenres().add(new BookGenre(book, new Genre(reader.getElementText())));
                } else if ("sequence".equals(localPart)) {
                    String sequenceName = element.getAttributeByName(new QName("", "name")).getValue();
                    Integer order;
                    try {
                        Attribute numberAttr = element.getAttributeByName(new QName("", "number"));
                        order = numberAttr == null ? 0 : Integer.valueOf(numberAttr.getValue());
                    } catch (NumberFormatException e) {
                        order = 0;
                        log.warn("sequence {} without order, book: {}", sequenceName, book.getTitle());
                    }
                    book.getSequences().add(new BookSequence(order, new Sequence(sequenceName)));
                }

            }
        }
        return null;
    }

    private String getText(XMLEventReader reader, String tag) throws XMLStreamException {
        StringBuilder text = new StringBuilder();
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isCharacters()) {
                String data = event.asCharacters().getData().trim();
                if (!data.isEmpty()) {
                    text.append(data).append("\n");
                }
            } else if (event.isEndElement() && tag.equals(event.asEndElement().getName().getLocalPart())) {
                return text.toString();

            }
        }

        return text.toString();
    }

    private Author parseAuthor(XMLEventReader reader) throws XMLStreamException {
        String lastName = "";
        String firstName = "";
        String middleName = "";
        Author author = new Author();
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String localPart = element.getName().getLocalPart();
                if ("first-name".equals(localPart)) {
                    firstName = reader.getElementText();
                } else if ("middle-name".equals(localPart)) {
                    middleName = reader.getElementText();
                } else if ("last-name".equals(localPart)) {
                    lastName = reader.getElementText();
                }
            }
            if (event.isEndElement() && "author".equals(event.asEndElement().getName().getLocalPart())) {
                String name = lastName + " " + firstName + " " + middleName;
                author.setName(name.replaceAll("\\s+", " "));
                return author;
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException {
        try {
            XMLEventReader reader = factory.createXMLEventReader(is);
            return new CloseableIterator()  {
                private String next;

                {
                    while (reader.hasNext()) {
                        XMLEvent event = reader.nextEvent();
                        if (event.isStartElement() &&
                                "body".equals(event.asStartElement().getName().getLocalPart())) {
                            break;
                        }
                    }
                    this.next = calcNext();
                }

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public String next() {
                    String next = this.next;
                    this.next = calcNext();
                    return next;
                }

                private String calcNext() {
                    try {
                        while (reader.hasNext()) {
                            XMLEvent event = reader.nextEvent();
                            if (event.isEndElement() && "body".equals(event.asEndElement().getName().getLocalPart())) {
                                return null;
                            } else if (event.isStartElement() && "p".equals(event.asStartElement().getName().getLocalPart())) {
                                return getText(reader, "p");
                            }
                        }
                        reader.close();
                        is.close();
                    } catch (XMLStreamException | IOException e) {
                        throw new LibException(e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public void close() throws IOException {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        throw new IOException(e);
                    }
                    is.close();
                }
            };
        } catch (XMLStreamException e) {
            throw new LibException(e.getMessage(), e);
        }
    }
}
