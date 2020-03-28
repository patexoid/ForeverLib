package com.patex.service;

import com.patex.entities.AuthorBookEntity;
import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import com.patex.entities.BookEntity;
import com.patex.mapper.BookMapper;
import com.patex.zombie.model.BookImage;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.FileResource;
import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookService bookService;
    private final AuthorRepository authorService;
    private final TransactionService transactionService;
    private final ParserService parserService;
    private final ApplicationEventPublisher publisher;
    private final BookMapper bookMapper;


    public void updateCovers() {
        bookService.findAll().
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

    public void publisEventForExistingBooks(User user) {
        bookService.findAll().
                filter(book -> !book.isDuplicate()).
                map(book -> new BookCreationEvent(book, user)).
                forEach(publisher::publishEvent);
    }

    public void checkDuplicatesForAuthor(User user, Long authorId) {
        transactionService.transactionRequired(
                () -> {
                    List<BookEntity> books = authorService.findById(authorId).stream().map(AuthorEntity::getBooks).
                            flatMap(Collection::stream).
                            map(AuthorBookEntity::getBook).collect(Collectors.toList());
                    books.forEach(book -> book.setDuplicate(false));
                    books.stream().map(book -> new BookCreationEvent(bookMapper.toDto(book), user)).
                            forEach(publisher::publishEvent);
                }
        );
    }
}
