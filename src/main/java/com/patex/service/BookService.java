package com.patex.service;

import com.patex.LibException;
import com.patex.entities.*;
import com.patex.parser.ParserService;
import com.patex.storage.LocalFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Created by Alexey on 12.03.2016.
 */
@Service
public class BookService {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    FileResourceRepository fileResourceRepository;

    @Autowired
    ParserService parserService;

    @Autowired
    LocalFileStorage fileStorage;

    public Book uploadBook(String fileName, InputStream is) throws LibException {

        byte[] byteArray = loadFromStream(is);
        Book book = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));
        String fileId = fileStorage.save(fileName, byteArray);
        FileResource fileResource = new FileResource(fileId);
        fileResource = fileResourceRepository.save(fileResource);
        book.setFileResource(fileResource);
        book.setFileName(fileName);
        book.setSize(byteArray.length);
        book = bookRepository.save(book);
        return book;
    }

    public Book saveBook(Book book){
        book.getFileResource().setDraft(false);
        book = bookRepository.save(book);
        return book;
    }

    private byte[] loadFromStream(InputStream is) throws LibException {
        byte[] buffer = new byte[32768];
        byte[] byteArray;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while (true) {
                int readBytesCount = is.read(buffer);
                if (readBytesCount == -1) {
                    break;
                }
                if (readBytesCount > 0) {
                    baos.write(buffer, 0, readBytesCount);
                }
            }
            baos.flush();
            byteArray = baos.toByteArray();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return byteArray;
    }

    public Book getBook(long id) {
        return bookRepository.findOne(id);
    }

    public InputStream getBookInputStream(Book book) throws LibException {
        return fileStorage.load(book.getFileResource().getFilePath());
    }
}
