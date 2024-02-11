package com.patex.forever.parser;

import com.patex.forever.entities.AuthorBookEntity;
import com.patex.forever.LibException;
import com.patex.forever.entities.AuthorEntity;
import com.patex.forever.entities.BookEntity;
import com.patex.forever.entities.BookGenreEntity;
import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.GenreEntity;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.model.BookImage;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Service
public class Fb2FileParser implements FileParser {

    private static final Logger log = LoggerFactory.getLogger(Fb2FileParser.class);

    private final XMLInputFactory factory;

    public Fb2FileParser() {
        factory = XMLInputFactory.newInstance();
    }

    @Override
    public String getExtension() {
        return "fb2";
    }


    @Override
    public BookInfo parseFile(String fileName, InputStream is, boolean parseBody) throws LibException {
        try {
            XMLEventReader reader = factory.createXMLEventReader(is);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement() && "title-info".equals(event.asStartElement().getName().getLocalPart())) {
                    return parseTitleInfo(reader, fileName, parseBody);
                }
            }
        } catch (XMLStreamException e) {
            throw new LibException(e.getMessage(), e);
        }
        throw new LibException("unable to parse fb2 file");
    }

    private BookInfo parseTitleInfo(XMLEventReader reader, String fileName, boolean parseBody) {
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBook(new BookEntity());
        try {
            parseTitleInfo(reader, bookInfo);
        } catch (XMLStreamException e) {
            log.error("Can't parse title for:" + fileName + " errorMessage:" + e.getMessage());
        }
        if (parseBody) {
            try {
                parseBodyAndBinary(reader, bookInfo);
            } catch (Exception e) {
                log.error("Can't parse body for:" + fileName + " errorMessage:" + e.getMessage());
            }
        }
        return bookInfo;
    }

    private void parseBodyAndBinary(XMLEventReader reader, BookInfo bookInfo) throws XMLStreamException {
        bookInfo.getBook().setContentSize(0);
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                if ("body".equals(event.asStartElement().getName().getLocalPart())) {
                    Integer contentSize = bookInfo.getBook().getContentSize();
                    int bodySize = getBodyContentSize(reader);
                    bookInfo.getBook().setContentSize(contentSize + bodySize);
                } else if ("binary".equals(event.asStartElement().getName().getLocalPart())) {
                    String id = event.asStartElement().getAttributeByName(QName.valueOf("id")).getValue();
                    String type = event.asStartElement().getAttributeByName(QName.valueOf("content-type")).getValue();
                    if (bookInfo.getCoverage() != null && bookInfo.getCoverage().contains(id) && type.contains("image")) {
                        String binary = getText(reader, "binary").replaceAll("\n", "");
                        try {
                            byte[] imageBytes = Base64.getDecoder().decode(binary);
                            BookImage bookImage = new BookImage();
                            bookImage.setImage(imageBytes);
                            bookImage.setType(type);
                            bookInfo.setBookImage(bookImage);
                        } catch (Exception e) {
                            log.error("Can't parse cover for book:{}", bookInfo.getBook().getTitle());
                        }
                    }
                }
            }
        }
    }

    private int getBodyContentSize(XMLEventReader reader) {
        Iterator<String> bodyIterator = createBodyIterator(reader);
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(bodyIterator, 0), false).
                map(String::length).reduce(Integer::sum).orElse(0);
    }

    private void parseTitleInfo(XMLEventReader reader, BookInfo bookInfo) throws XMLStreamException {
        BookEntity book = bookInfo.getBook();
        book.setLang("unknown");
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && "title-info".equals(event.asEndElement().getName().getLocalPart())) {
                break;
            } else if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String localPart = element.getName().getLocalPart();
                if ("author".equals(localPart)) {
                    AuthorEntity author = parseAuthor(reader);
                    if (author != null &&
                            book.getAuthorBooks().stream().map(AuthorBookEntity::getAuthor).map(AuthorEntity::getName).noneMatch(s -> StringUtils.equals(s, author.getName()))) {
                        book.addAuthor(author);
                    }
                } else if ("coverpage".equals(localPart)) {
                    bookInfo.setCoverage(getImageCoverage(reader));
                } else if ("book-title".equals(localPart)) {
                    book.setTitle(reader.getElementText());
                } else if ("lang".equals(localPart)) {
                    String value = reader.getElementText();
                    if (value != null) {
                        book.setLang(value.toLowerCase(Locale.ROOT));
                        book.setLangFb2(value);
                    }
                } else if ("src-lang".equals(localPart)) {
                    String value = reader.getElementText();
                    if (value != null) {
                        book.setSrcLang(value.toLowerCase(Locale.ROOT));
                    }
                } else if ("annotation".equals(localPart)) {
                    book.setDescr(getText(reader, "annotation"));
                } else if ("genre".equals(localPart)) {
                    String genreName = reader.getElementText();
                    if (genreName != null &&
                            book.getGenres().stream().map(BookGenreEntity::getGenre).map(GenreEntity::getName).noneMatch(s -> StringUtils.equals(s, genreName))) {
                        book.getGenres().add(new BookGenreEntity(book, new GenreEntity(genreName)));
                    }
                } else if ("sequence".equals(localPart)) {
                    String sequenceName = element.getAttributeByName(new QName("", "name")).getValue();
                    int order;
                    Attribute numberAttr = element.getAttributeByName(new QName("", "number"));
                    try {
                        order = numberAttr == null ? 0 : Integer.parseInt(numberAttr.getValue());
                    } catch (NumberFormatException e) {
                        order = 0;
                        log.warn("sequence {} with wrong order, book: {}, order {}", sequenceName, book.getTitle(), numberAttr);
                    }
                    book.getSequences().add(new BookSequenceEntity(order, new SequenceEntity(sequenceName)));
                }
            }
        }
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
                return text.length() == 0 ? "" : text.deleteCharAt(text.length() - 1).toString();
            }
        }
        return text.toString();
    }

    private AuthorEntity parseAuthor(XMLEventReader reader) throws XMLStreamException {
        String lastName = null;
        String firstName = null;
        String middleName = null;
        AuthorEntity author = new AuthorEntity();
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String localPart = element.getName().getLocalPart();
                if ("first-name".equals(localPart)) {
                    firstName = reader.getElementText().trim();
                } else if ("middle-name".equals(localPart)) {
                    middleName = reader.getElementText().trim();
                } else if ("last-name".equals(localPart)) {
                    lastName = reader.getElementText().trim();
                }
            }
            if (event.isEndElement() && "author".equals(event.asEndElement().getName().getLocalPart())) {
                String name = "";
                if (StringUtils.isNotEmpty(lastName)) {
                    name += lastName;
                }
                if (StringUtils.isNotEmpty(firstName)) {
                    if (!name.isEmpty()) {
                        name += " ";
                    }
                    name += firstName;
                }
                if (StringUtils.isNotEmpty(middleName)) {
                    if (!name.isEmpty()) {
                        name += " ";
                    }
                    name += middleName;
                }
                author.setName(name);
                return author;
            }
        }
        return null;
    }

    private String getImageCoverage(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String localPart = element.getName().getLocalPart();
                if ("image".equals(localPart)) {
                    QName hrefQName = new QName("http://www.w3.org/1999/xlink", "href");
                    Attribute href = element.getAttributeByName(hrefQName);
                    return href.getValue();
                }
            }
            if (event.isEndElement() && "coverage".equals(event.asEndElement().getName().getLocalPart())) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getContentIterator(String fileName, InputStream is) throws LibException {
        try {
            XMLEventReader reader = factory.createXMLEventReader(is);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement() &&
                        "body".equals(event.asStartElement().getName().getLocalPart())) {
                    break;
                }
            }
            Iterator<String> bodyIterator = createBodyIterator(reader);
            return new CloseableIterator() {
                Iterator<String> iterator = bodyIterator;

                @Override
                public void close() {
                    try {
                        reader.close();
                        is.close();
                    } catch (XMLStreamException | IOException e) {
                        throw new LibException(e);
                    }
                }

                @Override
                public boolean hasNext() {
                    boolean hasNext = iterator.hasNext();
                    if (!hasNext) {
                        try {
                            while (reader.hasNext()) {
                                XMLEvent event = reader.nextEvent();
                                if (event.isStartElement() &&
                                        "body".equals(event.asStartElement().getName().getLocalPart())) {
                                    iterator = createBodyIterator(reader);
                                    return hasNext();
                                }
                            }
                        } catch (XMLStreamException e) {
                            log.warn(e.getMessage(), e);
                        }
                        close();
                    }
                    return hasNext;
                }

                @Override
                public String next() {
                    return iterator.next();
                }
            };
        } catch (XMLStreamException e) {
            throw new LibException(e.getMessage(), e);
        }
    }

    private Iterator<String> createBodyIterator(XMLEventReader reader) {
        return new Iterator<>() {
            private String next = calcNext();

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
                } catch (XMLStreamException e) {
                    throw new LibException(e.getMessage(), e);
                }
                return null;
            }
        };
    }
}
