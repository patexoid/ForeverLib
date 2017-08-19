package com.patex.service;

import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookRepository;
import com.patex.entities.FileResource;
import com.patex.entities.Sequence;
import com.patex.parser.ParserService;
import com.patex.storage.StorageService;
import com.patex.utils.StreamU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.patex.service.ZUserService.ADMIN_AUTHORITY;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 *
 *
 */
@Service
public class BookService {
    private static Logger log = LoggerFactory.getLogger(BookService.class);


    private final BookRepository bookRepository;
    private final SequenceService sequenceService;
    private final AuthorService authorService;
    private final ParserService parserService;
    private final StorageService fileStorage;
    private final ZUserService userService;
    private final TransactionService transactionService;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public BookService(BookRepository bookRepository, SequenceService sequenceService,
                       AuthorService authorService, ParserService parserService, StorageService fileStorage,
                       ZUserService userService, TransactionService transactionService, ApplicationEventPublisher publisher) {
        this.bookRepository = bookRepository;
        this.sequenceService = sequenceService;
        this.authorService = authorService;
        this.parserService = parserService;
        this.fileStorage = fileStorage;
        this.userService = userService;
        this.transactionService = transactionService;
        this.publisher = publisher;
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public synchronized Book uploadBook(String fileName, InputStream is) throws LibException {
        Book result = transactionService.newTransaction(() -> {
            byte[] byteArray = loadFromStream(is);
            byte[] checksum = getChecksum(byteArray);
            Book book = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));
            Optional<Book> sameBook = bookRepository.findByTitleIgnoreCase(book.getTitle()).
                    stream().
                    filter(loaded -> Arrays.equals(checksum, loaded.getChecksum())).
                    findAny();
            if (sameBook.isPresent()) { //TODO if author or book has the same name
                return sameBook.get();
            }
            List<AuthorBook> authorsBooks = book.getAuthorBooks().stream().
                    map(authorBook -> {
                        List<Author> saved = authorService.findByName(authorBook.getAuthor().getName());
                        return saved.size() > 0 ? new AuthorBook(saved.get(0), book) : authorBook;
                    }).collect(Collectors.toList());
            book.setAuthorBooks(authorsBooks);

            Map<String, Sequence> sequencesMap = authorsBooks.stream().
                    map(AuthorBook::getAuthor).
                    flatMap(Author::getSequencesStream).
                    filter(sequence -> sequence.getId() != null). //already saved
                    filter(StreamU.distinctByKey(Sequence::getId)).
                    // some magic if 2 authors wrote the same sequence but different books
                            collect(Collectors.groupingBy(Sequence::getName, Collectors.toList())).
                            entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> sequenceService.mergeSequences(e.getValue())));


            book.getSequences().forEach(bookSequence -> {
                Sequence sequence = bookSequence.getSequence();
                bookSequence.setSequence(sequencesMap.getOrDefault(sequence.getName(), sequence));
                bookSequence.setBook(book);
            });

            String fileId = fileStorage.save(fileName, byteArray);
            FileResource fileResource = new FileResource(fileId);
            book.setFileResource(fileResource);
            book.setFileName(fileName);
            book.setContentSize(getContentSize(new ByteArrayInputStream(byteArray), fileName));
            book.setSize(byteArray.length);
            book.setChecksum(checksum);
            Book save = bookRepository.save(book);
            book.getAuthorBooks().stream().
                    filter(authorBook -> !authorBook.getAuthor().getBooks().contains(authorBook)).
                    forEach(authorBook -> authorBook.getAuthor().getBooks().add(authorBook));
            return save;
        });
        publisher.publishEvent(new BookCreationEvent(result, userService.getCurrentUser()));
        return result;
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

    public Book updateBook(Book book) throws LibException {
        if (bookRepository.exists(book.getId())) {
            return bookRepository.save(book);
        }
        throw new LibException("Book not found");
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

    private Integer getContentSize(InputStream is, String fileName) {
        Iterator<String> it = parserService.getContentIterator(fileName, is);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false).
                map(String::length).
                reduce((l1, l2) -> l1 + l2).orElse(0);
    }

    @Secured(ADMIN_AUTHORITY)
    public void updateContentSize() {
        transactionService.newTransaction(() -> {
            Iterable<Book> all = bookRepository.findAll();
            for (Book book : all) {
                try {
                    if (book.getContentSize() == null) {
                        InputStream is = fileStorage.load(book.getFileResource().getFilePath());
                        book.setContentSize(getContentSize(is, book.getFileName()));
                        bookRepository.save(book);
                    }
                } catch (Exception e) {
                    log.error("Error on contentSize calculation book:" + book.getId() + "title " + book.getTitle(), e);
                }
            }
        });
    }


    public Iterable<Book> getBooks() {
        return bookRepository.findAll();
    }

    @Secured(ADMIN_AUTHORITY)
    public void prepareExisted() {
        updateContentSize();
        Iterable<Book> books = bookRepository.findAll();
        for (Book book : books) {
            publisher.publishEvent(new BookCreationEvent(book, userService.getCurrentUser()));
        }
    }
}
