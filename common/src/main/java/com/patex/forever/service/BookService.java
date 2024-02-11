package com.patex.forever.service;

import com.patex.forever.LibException;
import com.patex.forever.model.Book;
import com.patex.forever.model.SimpleBook;
import com.patex.forever.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface BookService {
    Book uploadBook(String fileName, InputStream is, User user) throws LibException;

    Book uploadBook(String fileName, byte[] bytes, User user) throws LibException;

    Optional<Book> getBook(long id);

    Optional<SimpleBook> getSimpleBook(long id);

    InputStream getBookInputStream(Book book) throws LibException;

    InputStream getBookCoverInputStream(Book book) throws LibException;

    Page<Book> getBooks(Pageable pageable);

    Book updateBook(Book book) throws LibException;

    Book save(Book entity);


    Page<Book> getNewBooks(PageRequest pageRequest);

    List<SimpleBook> getSameAuthorsBook(SimpleBook primaryBook);

    String getPartialBookContent(String fileName, InputStream bookIS);
}
