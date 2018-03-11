package com.patex.service;

import com.patex.entities.Book;
import com.patex.entities.FileResource;
import com.patex.entities.ZUser;
import com.patex.parser.BookImage;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.stream.StreamSupport;

@Service
public class AdminService {

    private final BookService bookService;
    private final TransactionService transactionService;
    private final ParserService parserService;
    private final ApplicationEventPublisher publisher;

    public AdminService(BookService bookService, TransactionService transactionService, ParserService parserService, ApplicationEventPublisher publisher) {
        this.bookService = bookService;
        this.transactionService = transactionService;
        this.parserService = parserService;
        this.publisher = publisher;
    }

    public void updateCovers() {
        Iterable<Book> books = bookService.findAll();
        StreamSupport.stream(books.spliterator(), false).
                filter(book -> book.getCover() == null).forEach(
                book -> transactionService.newTransaction(() -> updateCover(book))
        );

    }

    private void updateCover(Book book) {
        InputStream bookIs = bookService.getBookInputStream(book);
        String fileName = book.getFileName();
        BookInfo bookInfo = parserService.getBookInfo(fileName, bookIs);
        BookImage bookImage = bookInfo.getBookImage();
        if (bookImage != null) {
            String cover = bookService.saveCover(fileName, bookImage);
            book.setCover(new FileResource(cover, bookImage.getType(), bookImage.getImage().length));
        }
        bookService.save(book);
    }

    public void publisEventForExistingBooks(ZUser user) {
        Iterable<Book> books = bookService.findAll();
        StreamSupport.stream(books.spliterator(), false).
                filter(Book::isDuplicate).
                map(book -> new BookCreationEvent(book, user)).
                forEach(publisher::publishEvent);
    }
}
