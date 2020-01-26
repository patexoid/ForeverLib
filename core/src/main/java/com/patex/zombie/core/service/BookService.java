package com.patex.zombie.core.service;

import com.patex.LibException;
import com.patex.zombie.core.entities.AuthorBookEntity;
import com.patex.zombie.core.entities.AuthorEntity;
import com.patex.zombie.core.entities.BookEntity;
import com.patex.zombie.core.entities.BookRepository;
import com.patex.zombie.core.entities.BookSequenceEntity;
import com.patex.zombie.core.entities.FileResourceEntity;
import com.patex.zombie.core.entities.SequenceEntity;
import com.patex.zombie.core.mapper.BookMapper;
import com.patex.model.Book;
import com.patex.model.User;
import com.patex.zombie.core.parser.BookImage;
import com.patex.zombie.core.parser.BookInfo;
import com.patex.zombie.core.parser.ParserService;
import com.patex.zombie.core.storage.StorageService;
import com.patex.zombie.core.utils.StreamU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final SequenceService sequenceService;
    private final AuthorService authorService;
    private final ParserService parserService;
    private final StorageService fileStorage;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher publisher;
    private final BookMapper mapper;

    @Autowired
    public BookService(BookRepository bookRepository, SequenceService sequenceService,
                       AuthorService authorService, ParserService parserService, StorageService fileStorage,
                       TransactionService transactionService, ApplicationEventPublisher publisher, BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.sequenceService = sequenceService;
        this.authorService = authorService;
        this.parserService = parserService;
        this.fileStorage = fileStorage;
        this.transactionService = transactionService;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    public BookEntity uploadBook(String fileName, InputStream is, User user) throws LibException {
        byte[] byteArray = loadFromStream(is);
        byte[] checksum = getChecksum(byteArray);
        BookInfo bookInfo = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));
        BookEntity book = bookInfo.getBook();
        BookEntity result = transactionService.transactionRequired(() -> {
            Optional<BookEntity> sameBook = bookRepository.findFirstByTitleAndChecksum(book.getTitle(), checksum);
            if (sameBook.isPresent()) {
                return sameBook.get();
            }
            log.trace("new book:{}", book.getFileName());
            List<AuthorEntity> authors = book.getAuthorBooks().stream().
                    map(AuthorBookEntity::getAuthor).
                    map(author -> authorService.findFirstByNameIgnoreCase(author.getName()).orElse(author)).
                    collect(Collectors.toList());
            List<AuthorBookEntity> authorsBooks = authors.stream().
                    map(author -> new AuthorBookEntity(author, book)).collect(Collectors.toList());
            book.setAuthorBooks(authorsBooks);

            Map<String, List<SequenceEntity>> sequenceMapList = authors.stream().
                    flatMap(AuthorEntity::getSequencesStream).
                    filter(sequence -> sequence.getId() != null). //already saved
                    filter(StreamU.distinctByKey(SequenceEntity::getId)).
                    collect(Collectors.groupingBy(SequenceEntity::getName, Collectors.toList()));
            // some magic if 2 authors wrote the same sequence but different books
            Map<String, SequenceEntity> sequencesMap = sequenceMapList.entrySet().stream().
                    collect(Collectors.toMap(Map.Entry::getKey, e -> sequenceService.mergeSequences(e.getValue())));


            List<BookSequenceEntity> sequences = book.getSequences().stream().
                    map(bs -> {
                        SequenceEntity sequence = bs.getSequence();
                        return new BookSequenceEntity(bs.getSeqOrder(),
                                sequencesMap.getOrDefault(sequence.getName(), sequence), book);
                    }).collect(Collectors.toList());
            book.setSequences(sequences);

            String fileId = fileStorage.save(byteArray, "book", fileName);
            book.setFileResource(new FileResourceEntity(fileId, "application/fb2+zip", byteArray.length));//TODO improve me
            BookImage bookImage = bookInfo.getBookImage();
            if (bookImage != null) {
                String cover = saveCover(fileName, bookImage);
                book.setCover(new FileResourceEntity(cover, bookImage.getType(), bookImage.getImage().length));
            }
            book.setFileName(fileName);
            book.setChecksum(checksum);
            book.setCreated(Instant.now());
            BookEntity save = bookRepository.save(book);
            someMagic(book);
            return save;
        });
        publisher.publishEvent(new BookCreationEvent(result, user));
        return result;
    }

    private void someMagic(BookEntity book) {
        book.getAuthorBooks().stream().
                filter(authorBook -> !authorBook.getAuthor().getBooks().contains(authorBook)).
                forEach(authorBook -> {
                    List<AuthorBookEntity> books = new ArrayList<>(authorBook.getAuthor().getBooks());
                    books.add(authorBook);
                    authorBook.getAuthor().setBooks(books);
                });
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

    public BookEntity getBook(long id) {
        return bookRepository.findById(id).get();
    }

    public InputStream getBookInputStream(BookEntity book) throws LibException {
        return fileStorage.load(book.getFileResource().getFilePath());
    }

    public InputStream getBookCoverInputStream(BookEntity book) throws LibException {
        return fileStorage.load(book.getCover().getFilePath());
    }

    public Page<BookEntity> getBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public BookEntity updateBook(Book book) throws LibException {
        return transactionService.transactionRequired(() ->
                bookRepository.findById(book.getId()).
                        map(bookEntity -> mapper.updateEntity(book, bookEntity)).
                        orElseThrow(() -> new LibException("BookEntity not found")));
    }

    private byte[] getChecksum(byte[] bookByteArray) throws LibException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new LibException(e);
        }
        digest.update(bookByteArray);
        return digest.digest();
    }


    String saveCover(String fileName, BookImage bookImage) {
        String coverName = fileName;
        String[] type = bookImage.getType().split("/");
        if (type.length > 1) {
            coverName = fileName + "." + type[1];
        }
        return fileStorage.save(bookImage.getImage(), "image", coverName);
    }

    public Stream<BookEntity> findAll() {
        return StreamSupport.stream(bookRepository.findAll().spliterator(), false);
    }

    public Page<BookEntity> getNewBooks(PageRequest pageRequest) {
        return bookRepository.findAllByOrderByCreatedDesc(pageRequest);
    }
}