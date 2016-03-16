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
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by alex on 15.03.2015.
 */
@Service
public class ZipFileParser implements FileParser {

    @Autowired
    ParserService parserService;

    @PostConstruct
    public void register() {
        parserService.registerParser(this);
    }

    @Override
    public String getExtension() {
        return "zip";
    }

    @Override
    public synchronized Book parseFile(String fileName, InputStream file)throws LibException {
        try(ZipInputStream zis= new ZipInputStream(file)) {
            zis.getNextEntry();
            return parserService.getBookInfo(fileName.substring(0, fileName.lastIndexOf('.')), zis);
        } catch (IOException e){
            throw new LibException(e);
        }
    }
}
