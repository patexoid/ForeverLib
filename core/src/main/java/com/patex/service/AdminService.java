package com.patex.service;

import com.patex.controllers.BookController;
import com.patex.entities.AuthorBookEntity;
import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import com.patex.entities.BookEntity;
import com.patex.entities.BookRepository;
import com.patex.mapper.BookMapper;
import com.patex.model.CheckDuplicateMessage;
import com.patex.zombie.model.BookImage;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookService bookService;

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;
    private final AuthorRepository authorService;
    private final TransactionService transactionService;
    private final ParserService parserService;

    private final RabbitService rabbitService;


    public void updateCovers() {
        bookRepository.findAll().
                filter(book -> book.getCover() == null).map(bookMapper::toDto).forEach(
                        book -> transactionService.newTransaction(() -> updateCover(book))
                );

    }

    private void updateCover(Book book) {
        InputStream bookIs = bookService.getBookInputStream(book);
        String fileName = book.getFileName();
        BookInfo bookInfo = parserService.getBookInfo(fileName, bookIs);
        BookImage bookImage = bookInfo.getBookImage();
        if (bookImage != null) {
            rabbitService.updateBookCover(bookImage, book.getId());
        }
        bookService.save(book);
    }

    public void publisEventForExistingBooks(User user) {
        bookRepository.findAll().
                filter(book -> !book.isDuplicate()).
                map(book -> new CheckDuplicateMessage(book.getId(), user.getUsername())).
                forEach(rabbitService::checkDuplicate);
    }

    public void checkDuplicatesForAuthor(User user, Long authorId) {
        transactionService.transactionRequired(
                        () -> {
                            List<BookEntity> books = authorService.findById(authorId).stream().map(AuthorEntity::getBooks).
                                    flatMap(Collection::stream).
                                    map(AuthorBookEntity::getBook).toList();
                            books.forEach(book -> book.setDuplicate(false));
                            return books;
                        }
                ).stream().map(book -> new CheckDuplicateMessage(book.getId(), user.getUsername())).
                forEach(rabbitService::checkDuplicate);
    }
}
