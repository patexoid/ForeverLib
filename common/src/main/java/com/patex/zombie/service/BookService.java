package com.patex.zombie.service;

import com.patex.zombie.LibException;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookImage;
import com.patex.zombie.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface BookService {
    Book uploadBook(String fileName, InputStream is, User user) throws LibException;

    Optional<Book> getBook(long id);

    InputStream getBookInputStream(Book book) throws LibException;

    InputStream getBookCoverInputStream(Book book) throws LibException;

    Page<Book> getBooks(Pageable pageable);

    Book updateBook(Book book) throws LibException;

    String saveCover(String fileName, BookImage bookImage);

    Book save(Book entity);

    Stream<Book> findAll();

    Page<Book> getNewBooks(PageRequest pageRequest);

    List<Book> getSameAuthorsBook(Book primaryBook);
}
