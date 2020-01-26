package com.patex.zombie.core.service;

import com.patex.zombie.core.entities.AuthorBookEntity;
import com.patex.zombie.core.entities.AuthorEntity;
import com.patex.zombie.core.entities.BookEntity;
import com.patex.zombie.core.entities.FileResourceEntity;
import com.patex.model.User;
import com.patex.zombie.core.parser.BookImage;
import com.patex.zombie.core.parser.BookInfo;
import com.patex.zombie.core.parser.ParserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final BookService bookService;
    private final AuthorService authorService;
    private final TransactionService transactionService;
    private final ParserService parserService;
    private final ApplicationEventPublisher publisher;

    public AdminService(BookService bookService, AuthorService authorService,
                        TransactionService transactionService, ParserService parserService,
                        ApplicationEventPublisher publisher) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.transactionService = transactionService;
        this.parserService = parserService;
        this.publisher = publisher;
    }

    public void updateCovers() {
        transactionService.transactionRequired(() -> bookService.findAll().
                filter(book -> book.getCover() == null).forEach(
                this::updateCover
        ));

    }

    private void updateCover(BookEntity book) {
        InputStream bookIs = bookService.getBookInputStream(book);
        String fileName = book.getFileName();
        BookInfo bookInfo = parserService.getBookInfo(fileName, bookIs);
        BookImage bookImage = bookInfo.getBookImage();
        if (bookImage != null) {
            String cover = bookService.saveCover(fileName, bookImage);
            book.setCover(new FileResourceEntity(cover, bookImage.getType(), bookImage.getImage().length));
        }
    }

    public void publisEventForExistingBooks(User user) {
        bookService.findAll().
                filter(book -> !book.isDuplicate()).
                map(book -> new BookCreationEvent(book, user)).
                forEach(publisher::publishEvent);
    }

    public void checkDuplicatesForAuthor(User user, Long authorId) {
        transactionService.transactionRequired(
                () -> {
                    AuthorEntity author = authorService.getAuthor(authorId);
                    List<BookEntity> books = author.getBooks().stream().map(AuthorBookEntity::getBook).collect(Collectors.toList());
                    books.forEach(book -> book.setDuplicate(false));
                    books.stream().map(book -> new BookCreationEvent(book, user)).
                            forEach(publisher::publishEvent);
                }
        );
    }
}
