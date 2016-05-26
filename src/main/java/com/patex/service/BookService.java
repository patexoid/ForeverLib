package com.patex.service;

import com.patex.LibException;
import com.patex.entities.*;
import com.patex.parser.ParserService;
import com.patex.storage.LocalFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 12.03.2016.
 */
@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Autowired
    private ParserService parserService;

    @Autowired
    private LocalFileStorage fileStorage;

    public Book uploadBook(String fileName, InputStream is) throws LibException {

        byte[] byteArray = loadFromStream(is);
        Book book = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));

        List<Book> books = bookRepository.findByTitleIgnoreCase(book.getTitle()).
                stream().filter(loaded -> hasTheSameAuthors(book, loaded)).collect(Collectors.toList());

        if(books.size()>0){ //TODO if author or book has the same name
            return books.get(0);
        }
       List<Author> authors=book.getAuthors().stream().map(author -> {
            List<Author> saved = authorService.findByName(author.getName());
            return saved.size()>0?saved.get(0):author;
        }).collect(Collectors.toList());
        book.setAuthors(authors);

        Map<String,Sequence> sequencesMap=authors.stream().flatMap(Author::getSequences).
                collect(Collectors.toMap(Sequence::getName,sequence -> sequence));

        book.getSequences().stream().forEach(bookSequence -> {
            Sequence sequence = sequencesMap.get(bookSequence.getSequence().getName());
            bookSequence.setSequence(sequence==null?bookSequence.getSequence():sequence);
            bookSequence.setBook(book);

        });

        String fileId = fileStorage.save(fileName, byteArray);
        FileResource fileResource = new FileResource(fileId);
        fileResource = fileResourceRepository.save(fileResource);
        book.setFileResource(fileResource);
        book.setFileName(fileName);
        book.setSize(byteArray.length);
        return  bookRepository.save(book);
    }

    private static boolean hasTheSameAuthors(Book primary, Book secondary){
        Set<String> primaryAuthors=primary.getAuthors().stream().map(Author::getName).collect(Collectors.toSet());
        Set<String> secondaryAuthors= secondary.getAuthors().stream().map(Author::getName).collect(Collectors.toSet());
        return CollectionUtils.containsAny(primaryAuthors,secondaryAuthors);
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
