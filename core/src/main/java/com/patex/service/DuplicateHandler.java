package com.patex.service;


import com.patex.LibException;
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
import com.patex.utils.BlockingExecutor;
import com.patex.utils.StreamU;
import com.patex.utils.shingle.ShingleMatcher;
import com.patex.utils.shingle.Shingleable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private final ExecutorService scheduleExecutor;

    private final Semaphore lock = new Semaphore(0);
    private int threadCount;
    private BlockingExecutor blockingExecutor;

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
        this.threadCount = threadCount;
        if (threadCount == 0) {
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            this.threadCount = threadCount;
            this.threadCount = availableProcessors > 1 ? availableProcessors / 2 : 1;
        }
        scheduleExecutor = Executors.newSingleThreadExecutor(createThreadFactory(() -> "scheduleExecutor"));
        blockingExecutor = new BlockingExecutor(this.threadCount, this.threadCount * 5, 1,
                TimeUnit.MINUTES, this.threadCount * 5,
                createThreadFactory(() -> "checkForDuplicate-"));

    }

    private ThreadFactory createThreadFactory(Supplier<String> supplier) {
        return r -> {
            Thread thread = new Thread(r);
            thread.setName(supplier.get());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
            return thread;
        };
    }

    @PostConstruct
    public void postConstruct() {
        Thread scheduler = new Thread(() -> {
            try {
                taskScheduler();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

        });
        scheduler.setName("Duplicate handler scheduler");
        scheduler.setDaemon(true);
        scheduler.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
        scheduler.start();
    }

    private void taskScheduler() throws InterruptedException {
        long lastId = 0;
        int pageSize = threadCount * 10;
        //noinspection InfiniteLoopStatement
        while (true) {
            List<BookCheckQueue> checkQueue = bookCheckQueueRepo.
                    findAllByIdGreaterThanOrderByIdAsc(new PageRequest(0, pageSize), lastId).getContent();
            if (checkQueue.isEmpty()) {
                lock.acquire();
                lock.drainPermits();
            } else {
                lastId = checkQueue.get(checkQueue.size() - 1).getId();
                for (BookCheckQueue bcq : checkQueue) {
                    blockingExecutor.execute(
                            () -> transactionService.transactionRequired(() -> checkForDuplicate(bcq)));
                }
            }
        }
    }

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
        scheduleExecutor.execute(() -> {
            if (!transactionService.newTransaction(() -> scheduleCheck(event)).isEmpty())
                lock.release();
        });
    }

    private List<BookCheckQueue> scheduleCheck(BookCreationEvent event) {
        Book newBook = bookService.getBook(event.getBook().getId());
        List<BookCheckQueue> bookCheckQueues = newBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).
                flatMap(a -> a.getBooks().stream().map(AuthorBook::getBook)).
                filter(book -> !book.getId().equals(newBook.getId())).
                filter(StreamU.distinctByKey(Book::getId)).
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

        bookCheckQueues.forEach(bookCheckQueueRepo::saveAndFlush);
        return bookCheckQueues;

    }

    private BookCheckQueue checkForDuplicate(BookCheckQueue bookCheckQueue) {
        try {
            Book first = bookService.getBook(bookCheckQueue.getBook1().getId());
            Book second = bookService.getBook(bookCheckQueue.getBook2().getId());
            if (!first.isDuplicate() && !second.isDuplicate() &&
                    shingleMatcher.isSimilar(first, second)) {
                markDuplications(first, second, bookCheckQueue.getUser());
                log.trace("duplicate id=" + bookCheckQueue.getId());
            }
            bookCheckQueueRepo.delete(bookCheckQueue.getId());
            return bookCheckQueue;
        } catch (Exception e) {
            throw new LibException("Duplication check exception book " +
                    " first.id= " + bookCheckQueue.getBook1().getId() +
                    " first.title = " + bookCheckQueue.getBook1().getTitle() +
                    " first.filename" + bookCheckQueue.getBook1().getFileName() +
                    "\n second.id= " + bookCheckQueue.getBook2().getId() +
                    " second.title = " + bookCheckQueue.getBook2().getTitle() +
                    " second.filename" + bookCheckQueue.getBook2().getFileName() +
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
        shingleMatcher.invalidate(secondary);
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
            messenger.sendMessageToUser(user, message);
        }
        this.bookService.updateBook(primary);
    }

    private class ShingleableBook implements Shingleable {
        private final Book book;
        private Iterator<String> contentIterator;
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
            boolean hasNext = contentIterator.hasNext();
            if (!hasNext) {
                try {
                    close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            return hasNext;
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
