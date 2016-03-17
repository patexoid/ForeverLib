package com.patex.parser;

import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alex on 15.03.2015.
 */
@Service
public class Fb2FileParser implements FileParser {


    public static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();
    private DocumentBuilder builder;
    private XPath xpath;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD");

    public Fb2FileParser() {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xpath = X_PATH_FACTORY.newXPath();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
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
    public synchronized Book parseFile(String fileName, InputStream file) throws LibException{
        Document document;
        try {
            document = builder.parse(file);
        } catch (IOException | SAXException e) {
            throw new LibException(e.getMessage(),e);//TODO exception handling
        }

        try {
            Node description = (Node) xpath.evaluate("/FictionBook/description/title-info", document, XPathConstants.NODE);
            NodeList genreNodeList = (NodeList) xpath.evaluate("genre", description, XPathConstants.NODESET);
            List<String> genres = new ArrayList<String>();
            for (int i = 0; i < genreNodeList.getLength(); i++) {
                genres.add(genreNodeList.item(i).getTextContent());
            }

            Node authorNode = (Node) xpath.evaluate("author", description, XPathConstants.NODE);
            String firstName = (String) xpath.evaluate("first-name", authorNode, XPathConstants.STRING);
            String middleName = (String) xpath.evaluate("middle-name", authorNode, XPathConstants.STRING);
            String lastName = (String) xpath.evaluate("last-name", authorNode, XPathConstants.STRING);
            String homePage = (String) xpath.evaluate("home-page", authorNode, XPathConstants.STRING);
            String email = (String) xpath.evaluate("email", authorNode, XPathConstants.STRING);

            String title = (String) xpath.evaluate("book-title", description, XPathConstants.STRING);
            String annotation = (String) xpath.evaluate("annotation", description, XPathConstants.STRING);
            String dateString = (String) xpath.evaluate("date/@value", description, XPathConstants.STRING);
            Book book = new Book();
            book.setTitle(title);
            Author author = new Author(lastName + " " + firstName + " " + middleName);
            book.addAuthor(author);
            return book;

        } catch (XPathExpressionException e) {
            throw new LibException(e.getMessage(),e);
        }


    }
}
