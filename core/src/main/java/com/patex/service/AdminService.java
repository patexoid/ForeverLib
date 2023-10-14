package com.patex.service;

import com.google.common.collect.Lists;
import com.patex.entities.AuthorBookEntity;
import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import com.patex.entities.BookEntity;
import com.patex.entities.BookRepository;
import com.patex.entities.FileResourceEntity;
import com.patex.mapper.BookMapper;
import com.patex.model.CheckDuplicateMessage;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookImage;
import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.StorageService;
import com.patex.zombie.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final BookService bookService;

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    private final BookMapper bookMapper;
    private final AuthorRepository authorService;
    private final TransactionService transactionService;
    private final ParserService parserService;

    private final RabbitService rabbitService;

    private final LanguageService languagesService;
    private final StorageService storageService;

    public void updateCovers() {
        bookRepository.findAll().
                filter(book -> book.getCover() == null).map(bookMapper::toDto).forEach(
                        book -> transactionService.newTransaction(() -> updateCover(book))
                );

    }

    private void updateCover(Book book) {
        InputStream bookIs = bookService.getBookInputStream(book);
        String fileName = book.getFileName();
        BookInfo bookInfo = parserService.getBookInfo(fileName, bookIs, true);
        BookImage bookImage = bookInfo.getBookImage();
        if (bookImage != null) {
            rabbitService.updateBookCover(bookImage, book.getId());
        }
    }

    public void updateDuplicateInfoForAll(User user) {
        bookRepository.booksForDuplicateCheck().stream().
                map(id -> new CheckDuplicateMessage(id, user.getUsername())).
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

    public void updateBookLocation() {
        List<Long> ids = bookRepository.booksForDuplicateCheck();
        List<List<Long>> partitions = Lists.partition(ids, 1000);
        partitions.forEach(partIds -> {
            transactionService.transactionRequired(() -> {
                bookRepository.findByIdIn(partIds).forEach(be -> {
                    String[] filePath = BookServiceImpl.getFilePath(be, be.getFileName());
                    String moved = storageService.move(be.getFileResource().getFilePath(), filePath, true);
                    be.getFileResource().setFilePath(moved);

                    FileResourceEntity cover = be.getCover();
                    if (cover != null) {
                        String[] newpath = CoverService.getCoverPath(moved, cover.getType());
                        String newCoverPath = storageService.move(cover.getFilePath(), newpath, false);
                        cover.setFilePath(newCoverPath);
                    }
                });
            });
        });
    }

    @SneakyThrows
    public void updateLangAndSrcLang() {
        Page<Long> page;
        do {
            page = bookRepository.findAllByLangIsNull(Pageable.ofSize(5000)); // always take first page because lang was updated to non null
            List<Long> content = page.getContent();
            transactionService.transactionRequired(() -> updateLangAndSrcLang(content));
        } while (page.hasNext());
        transactionService.transactionRequired(() -> authorRepository.updateLang());

    }

    private void updateLangAndSrcLang(List<Long> books) {
        bookRepository.findByIdIn(books).forEach(book -> {
            String fileName;
            BookInfo bookInfo;
            try (InputStream bookIs = bookService.getBookInputStream(bookMapper.toDto(book))) {
                fileName = book.getFileName();
                bookInfo = parserService.getBookInfo(fileName, bookIs, false);
                book.setLang(bookInfo.getBook().getLang());
                book.setLangFb2(bookInfo.getBook().getLangFb2());
                book.setSrcLang(bookInfo.getBook().getSrcLang());
                languagesService.detectLang(book::getDescr,
                                ()->bookService.getPartialBookContent(fileName, book.getFileResource().getFilePath())).
                        ifPresent(book::setLang);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
