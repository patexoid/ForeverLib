package com.patex.service;

import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookCheckQueue;
import com.patex.entities.BookCheckQueueRepository;
import com.patex.entities.BookRepository;
import com.patex.entities.BookSequence;
import com.patex.entities.FileResource;
import com.patex.entities.Sequence;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.parser.ParserService;
import com.patex.storage.StorageService;
import com.patex.utils.StreamU;
import com.patex.utils.shingle.ShingleComparsion;
import com.patex.utils.shingle.Shingleable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final BookRepository bookRepository;
    private final SequenceService sequenceService;
    private final AuthorService authorService;
    private final ParserService parserService;
    private final StorageService fileStorage;
    private final BookCheckQueueRepository bookCheckQueueRepo;
    private final MessengerService messenger;
    private final ZUserService userService;
    private final TransactionService transactionService;

    @Autowired
    public BookService(BookRepository bookRepository, SequenceService sequenceService,
                       AuthorService authorService, ParserService parserService, StorageService fileStorage,
                       BookCheckQueueRepository bookCheckQueueRepo, MessengerService messenger,
                       ZUserService userService, TransactionService transactionService) {
        this.bookRepository = bookRepository;
        this.sequenceService = sequenceService;
        this.authorService = authorService;
        this.parserService = parserService;
        this.fileStorage = fileStorage;
        this.bookCheckQueueRepo = bookCheckQueueRepo;
        this.messenger = messenger;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public synchronized Book uploadBook(String fileName, InputStream is) throws LibException {
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
        bookCheckQueueRepo.save(new BookCheckQueue(book));
        ZUser currentUser = userService.getCurrentUser();
        executor.execute(() -> checkForDuplicate(currentUser));
        return save;
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

    @Secured(ADMIN_AUTHORITY)
    private void updateContentSize() {
        Iterable<Book> all = bookRepository.findAll();
        for (Book book : all) {
            try {
                if(book.getContentSize()==null) {
                InputStream is = fileStorage.load(book.getFileResource().getFilePath());
                book.setContentSize(getContentSize(is, book.getFileName()));
                bookRepository.save(book);
                }
            } catch (Exception e) {
                log.error("Error on contentSize calculation book:" + book.getId() + "title " + book.getTitle(), e);
            }
        }
    }

    private Integer getContentSize(InputStream is, String fileName) {
        Iterator<String> it = parserService.getContentIterator(fileName, is);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false).
                map(String::length).
                reduce((l1, l2) -> l1 + l2).orElse(0);
    }

    @Secured(ADMIN_AUTHORITY)
    public void checkForDuplicateSecured() {
        transactionService.newTransaction(this::updateContentSize);
        checkForDuplicate(userService.getCurrentUser());
    }

    private synchronized void checkForDuplicate(ZUser currentUser) {
        Iterable<BookCheckQueue> queue = bookCheckQueueRepo.findAll();
        for (BookCheckQueue bookCheckQueue : queue) {
            transactionService.newTransaction(()->checkForDuplicate(bookCheckQueue, currentUser));
        }
    }

    private void checkForDuplicate(BookCheckQueue bookCheckQueue, ZUser user) {
        Book checkedBook = bookRepository.findOne(bookCheckQueue.getBook().getId());
        try {
            if (!checkedBook.isDuplicate()) {
                findDuplications(checkedBook).
                        ifPresent(book -> markDuplications(checkedBook, book, user));
            }
            bookCheckQueueRepo.delete(bookCheckQueue.getId());
        } catch (Exception e) {
            log.error("Duplication check exception book " +
                    " id= " + checkedBook.getId() +
                    " title = " + checkedBook.getTitle() +
                    " filename" + checkedBook.getFileName() +
                    " exception=" + e.getMessage(), e);
        }
    }

    private void markDuplications(Book first, Book second, ZUser user) {
        Book primary, secondary;
        if (first.getContentSize() > second.getContentSize()) {
            primary = first;
            secondary = second;
        } else {
            primary = second;
            secondary = first;
        }

        List<Long> sequences = primary.getSequences().stream().
                map(BookSequence::getSequence).
                map(Sequence::getId).
                collect(Collectors.toList());
        List<Long> authors = primary.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(Author::getId).
                collect(Collectors.toList());

        secondary.setDuplicate(true);
        this.bookRepository.save(secondary);
        for (AuthorBook authorBook : secondary.getAuthorBooks()) {
            Author author = authorBook.getAuthor();
            if (!authors.contains(author.getId())) {
                primary.getAuthorBooks().add(new AuthorBook(author, primary));
            }
        }
        for (BookSequence bookSequence : secondary.getSequences()) {
            Sequence sequence = bookSequence.getSequence();
            if (!sequences.contains(sequence.getId())) {
                primary.getSequences().
                        add(new BookSequence(bookSequence.getSeqOrder(), sequence, primary));
            }
        }
        String message = "Book:" + first.getTitle() + "\nPrimary: " + primary.getTitle() + "\nDuplicate: " +
                secondary.getTitle();
        log.info(message);
        if (user != null) {
            messenger.sendMessageToUser(message, user);
        }
        this.bookRepository.save(primary);
    }

    private Optional<Book> findDuplications(Book checkedBook) {
        List<Book> sameAuthorBooks = checkedBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).
                flatMap(a -> a.getBooks().stream().map(AuthorBook::getBook)).
                filter(book -> !book.getId().equals(checkedBook.getId())).
                filter(book -> !book.isDuplicate()).
                filter(book -> {
                    float min = (float) Math.min(book.getContentSize(), checkedBook.getContentSize());
                    float max = (float) Math.min(book.getContentSize(), checkedBook.getContentSize());
                    return min / max > 0.7f;
                }).
                sorted(Comparator.comparing((book) -> StringUtils.getLevenshteinDistance(book.getTitle(), checkedBook.getTitle()))).
                collect(Collectors.toList());
        if(sameAuthorBooks.isEmpty()){
            return Optional.empty();
        } else {
            return new ShingleComparsion().findSimilar(checkedBook, sameAuthorBooks, ShingleableBook::new);
        }
    }

    private class ShingleableBook implements Shingleable, Closeable {
        private final Book book;
        private final Iterator<String> contentIterator;
        private InputStream is;

        ShingleableBook(Book book) {
            this.book = book;
            is = fileStorage.load(book.getFileResource().getFilePath());
            this.contentIterator = parserService.getContentIterator(book.getFileName(), is);
        }

        @Override
        public int size() {
            return book.getContentSize();
        }

        @Override
        public boolean hasNext() {
            return contentIterator.hasNext();
        }

        @Override
        public String next() {
            return contentIterator.next();
        }

        @Override
        public void close() throws IOException {
            is.close();
            if (contentIterator instanceof Closeable) {
                ((Closeable) contentIterator).close();
            }
        }
    }
}
