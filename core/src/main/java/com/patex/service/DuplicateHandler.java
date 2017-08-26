package com.patex.service;


import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookCheckQueue;
import com.patex.entities.BookCheckQueueRepository;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.parser.ParserService;
import com.patex.storage.StorageService;
import com.patex.utils.shingle.ShingleMatcher;
import com.patex.utils.shingle.Shingleable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.patex.service.ZUserService.ADMIN_AUTHORITY;

@Component
public class DuplicateHandler {

    private static Logger log = LoggerFactory.getLogger(DuplicateHandler.class);


    private final BookCheckQueueRepository bookCheckQueueRepo;
    private final TransactionService transactionService;
    private final BookService bookService;
    private final MessengerService messenger;
    private final StorageService fileStorage;
    private final ParserService parserService;
    private final ShingleMatcher<Book, Long> shingleMatcher = new ShingleMatcher<>(ShingleableBook::new, Book::getId);


    private static final AtomicInteger count = new AtomicInteger(0);

    private final ExecutorService executor;

    @Autowired
    public DuplicateHandler(BookCheckQueueRepository bookCheckQueueRepo, TransactionService transactionService,
                            BookService bookService, MessengerService messenger, StorageService fileStorage,
                            ParserService parserService,
                            @Value("${duplicateCheckThreadCount:0}") int threadCount) {
        this.bookCheckQueueRepo = bookCheckQueueRepo;
        this.transactionService = transactionService;
        this.bookService = bookService;
        this.messenger = messenger;
        this.fileStorage = fileStorage;
        this.parserService = parserService;

        if (threadCount == 0) {
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            threadCount = availableProcessors > 1 ? availableProcessors / 2 : 1;
        }
        executor = Executors.newFixedThreadPool(threadCount,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("DuplicateHandler-" + count.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
                    return thread;
                });
    }

    @PostConstruct
    public void postConstruct() {
        Thread scheduler = new Thread(() -> {
            Iterable<BookCheckQueue> checkQueue = bookCheckQueueRepo.findAll();
            for (BookCheckQueue bookCheckQueue : checkQueue) {
                log.trace("scheduleCheck book1={} book2={} id={}",
                        bookCheckQueue.getBook1().getTitle(), bookCheckQueue.getBook2().getTitle(), bookCheckQueue.getId());
                addCheckTask(bookCheckQueue);
            }
        });
        scheduler.setName("initital duplicate handler scheduler");
        scheduler.setDaemon(true);
        scheduler.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
        scheduler.start();
    }

    @Secured(ADMIN_AUTHORITY)
    public void waitForFinish() {
        while (true) {
            long count = bookCheckQueueRepo.count();
            log.trace("duplicateCheck count:" + count);
            if (count == 0) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    @EventListener
    public void onBookCreation(BookCreationEvent event) {
        executor.execute(() -> {
            List<BookCheckQueue> bookCheckQueues = transactionService.transactionRequired(() -> scheduleCheck(event));
            for (BookCheckQueue bookCheck : bookCheckQueues) {
                addCheckTask(bookCheck);
            }
        });
    }

    private List<BookCheckQueue> scheduleCheck(BookCreationEvent event) {
        Book newBook = bookService.getBook(event.getBook().getId());
        List<BookCheckQueue> bookCheckQueues = newBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).
                flatMap(a -> a.getBooks().stream().map(AuthorBook::getBook)).
                filter(book -> !book.getId().equals(newBook.getId())).
                filter(book -> !book.isDuplicate()).
                filter(book -> {
                    float min = (float) Math.min(book.getContentSize(), newBook.getContentSize());
                    float max = (float) Math.max(book.getContentSize(), newBook.getContentSize());
                    return min / max > 0.7f;
                }).
                sorted(Comparator.comparing((book) -> StringUtils.getLevenshteinDistance(book.getTitle(), newBook.getTitle()))).
                map(sameAuthorsBook -> {
                    if (newBook.getId() > sameAuthorsBook.getId()) {
                        return new BookCheckQueue(sameAuthorsBook, newBook, event.getUser());
                    } else {
                        return new BookCheckQueue(newBook, sameAuthorsBook, event.getUser());
                    }
                }).
                filter(bcq -> !bookCheckQueueRepo.
                        existsByBook1EqualsAndBook2Equals(bcq.getBook1(), bcq.getBook2())).collect(Collectors.toList());

        bookCheckQueueRepo.save(bookCheckQueues);
        return bookCheckQueues;

    }

    private void addCheckTask(BookCheckQueue bookCheckQueue) {
        executor.execute(() -> transactionService.newTransaction(() -> checkForDuplicate(bookCheckQueue)));
    }

    private void checkForDuplicate(BookCheckQueue bookCheckQueue) {
        Book first = bookService.getBook(bookCheckQueue.getBook1().getId());
        Book second = bookService.getBook(bookCheckQueue.getBook2().getId());
        log.trace("START scheduleCheck book1={} book2={}",
                bookCheckQueue.getBook1().getTitle(), bookCheckQueue.getBook2().getTitle(), bookCheckQueue.getId());
        try {
            if (shingleMatcher.isSimilar(first, second)) {
                markDuplications(first, second, bookCheckQueue.getUser());
            }
            log.trace("DONE scheduleCheck book1={} book2={} id={}",
                    bookCheckQueue.getBook1().getTitle(), bookCheckQueue.getBook2().getTitle(), bookCheckQueue.getId());
            bookCheckQueueRepo.delete(bookCheckQueue.getId());

        } catch (Exception e) {
            log.error("Duplication check exception book " +
                    " first.id= " + first.getId() +
                    " first.title = " + first.getTitle() +
                    " first.filename" + first.getFileName() +
                    "\n second.id= " + second.getId() +
                    " second.title = " + second.getTitle() +
                    " second.filename" + second.getFileName() +
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
                filter(Objects::nonNull).//todo delete later
                map(Sequence::getId).
                collect(Collectors.toList());
        List<Long> authors = primary.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(Author::getId).
                collect(Collectors.toList());

        secondary.setDuplicate(true);
        this.bookService.updateBook(secondary);
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
        this.bookService.updateBook(primary);
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
