package com.patex.service;

import com.patex.LibException;
import com.patex.entities.*;
import com.patex.parser.ParserService;
import com.patex.storage.StorageService;
import com.patex.utils.StreamU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private StorageService fileStorage;


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Book uploadBook(String fileName, InputStream is) throws LibException {

        byte[] byteArray = loadFromStream(is);
        Book book = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));

        List<Book> books = bookRepository.findByTitleIgnoreCase(book.getTitle()).
                stream().filter(loaded -> hasTheSameAuthors(book, loaded)).collect(Collectors.toList());

        if(books.size()>0){ //TODO if author or book has the same name
            return books.get(0);
        }
       List<AuthorBook> authorsBooks=book.getAuthorBooks().stream().map(authorBook -> {
            List<Author> saved = authorService.findByName(authorBook.getAuthor().getName());
            return saved.size()>0?new AuthorBook(saved.get(0),book):authorBook;
        }).collect(Collectors.toList());
        book.setAuthorBooks(authorsBooks);



        Map<String,Sequence> sequencesMap=authorsBooks.stream().
                map(AuthorBook::getAuthor).
                flatMap(Author::getSequencesStream).
                filter(sequence -> sequence.getId()!=null). //already saved
                filter(StreamU.distinctByKey(Sequence::getId)).
                collect(Collectors.toMap(Sequence::getName,sequence -> sequence));

        book.getSequences().forEach(bookSequence -> {
            Sequence sequence = sequencesMap.get(bookSequence.getSequence().getName());
            bookSequence.setSequence(sequence == null ? bookSequence.getSequence() : sequence);
            bookSequence.setBook(book);

        });

        String fileId = fileStorage.save(fileName, byteArray);
        FileResource fileResource = new FileResource(fileId);
//        fileResource = fileResourceRepository.save(fileResource);
        book.setFileResource(fileResource);
        book.setFileName(fileName);
        book.setSize(byteArray.length);
        Book save = bookRepository.save(book);
        book.getAuthorBooks().stream().
                filter(authorBook -> !authorBook.getAuthor().getBooks().contains(authorBook)).
                forEach(authorBook -> authorBook.getAuthor().getBooks().add(authorBook));
        return save;
    }

    private static boolean hasTheSameAuthors(Book primary, Book secondary){
        Set<String> primaryAuthors=primary.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(Author::getName).collect(Collectors.toSet());
        Set<String> secondaryAuthors= secondary.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(Author::getName).collect(Collectors.toSet());
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

    public Page<Book> getBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Book updateBook(Book book) throws LibException{
        if(bookRepository.exists(book.getId())){
            return bookRepository.save(book);
        }
        throw new LibException("Book not found");
    }
}
