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
import com.patex.shingle.ShingleCacheStorage;
import com.patex.shingle.ShingleSearch;
import com.patex.shingle.Shingleable;
import com.patex.storage.StorageService;
import com.patex.utils.BlockingExecutor;
import com.patex.utils.StreamU;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private final ShingleSearch<Book, Long> shingleSearch;
    private final ExecutorService scheduleExecutor;

    private final Semaphore lock = new Semaphore(0);
    private int threadCount;
    private BlockingExecutor blockingExecutor;

    @Autowired
    public DuplicateHandler(BookCheckQueueRepository bookCheckQueueRepo, TransactionService transactionService,
                            BookService bookService, MessengerService messenger, StorageService fileStorage,
                            ParserService parserService,
                            @Value("${duplicateCheck.threadCount:0}") int threadCount,
                            @Value("${duplicateCheck.shingleCoeff:1}") int coef,
                            @Value("${duplicateCheck.fastCacheSize:100}") int cacheSize,
                            @Value("${duplicateCheck.storageCacheFolder}") String  storageFolder) {
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
        shingleSearch = new ShingleSearch<>(this::getSameAuthorsBook,
                ShingleableBook::new,
                Book::getId,
                coef, cacheSize);
    if (StringUtils.isNotEmpty(storageFolder)) {
        shingleSearch.setStorage(new BookShingleCacheStorage(storageFolder));
    }
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
                            () -> transactionService.newTransaction(() -> checkForDuplicate(bcq)));
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
            if (transactionService.newTransaction(() -> scheduleCheck(event)))
                lock.release();
        });
    }

    private synchronized boolean scheduleCheck(BookCreationEvent event) {
        Book newBook = bookService.getBook(event.getBook().getId());
        if (newBook.isDuplicate()) {
            return false;
        } else {
            bookCheckQueueRepo.saveAndFlush(new BookCheckQueue(newBook, event.getUser()));
            return true;
        }
    }

    private List<Book> getSameAuthorsBook(Book primaryBook) {
        return primaryBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).
                flatMap(a -> a.getBooks().stream().map(AuthorBook::getBook)).
                filter(book -> !book.getId().equals(primaryBook.getId())).
                filter(StreamU.distinctByKey(Book::getId)).
                filter(book -> !book.isDuplicate()).
                sorted(Comparator.comparing(
                        book -> StringUtils.getLevenshteinDistance(book.getTitle(), primaryBook.getTitle()))).
                collect(Collectors.toList());

    }

    private BookCheckQueue checkForDuplicate(BookCheckQueue bookCheckQueue) {
        Book primary = bookService.getBook(bookCheckQueue.getBook().getId());
        try {
            shingleSearch.findSimilar(primary).ifPresent(
                    book -> {
                        markDuplications(primary, book, bookCheckQueue.getUser());
                    });
            bookCheckQueueRepo.delete(bookCheckQueue.getId());
            log.trace("duplicate id=" + bookCheckQueue.getId());
            return bookCheckQueue;
        } catch (Exception e) {
            throw new LibException("Duplication check exception book " +
                    " book.id= " + primary.getId() +
                    " book.title = " + primary.getTitle() +
                    " book.filename" + primary.getFileName() +
                    " exception=" + e.getMessage(),
                    e);
        }
    }

    private void markDuplications(Book first, Book second, ZUser user) {
        try {
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
            shingleSearch.invalidate(secondary);
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
        } catch (Exception e) {
            throw new LibException("Duplication mark exception book " +
                    " first.id= " + first.getId() +
                    " first.title = " + first.getTitle() +
                    " first.filename" + first.getFileName() +
                    "\n second.id= " + second.getId() +
                    " second.title = " + second.getTitle() +
                    " second.filename" + second.getFileName() +
                    " exception=" + e.getMessage(), e);
        }
    }

    private static class BookShingleCacheStorage implements ShingleCacheStorage<Book> {
        private final  String storageFolder;

        public BookShingleCacheStorage(String storageFolder) {
            this.storageFolder = storageFolder;
        }

        @Override
        public InputStream load(Book book) {
            File cache = getCacheFile(book);
            if (cache.exists()) {
                try {
                    return new FileInputStream(cache);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private File getCacheFile(Book book) {
            return new File(storageFolder + "/" + book.getId());
        }

        @Override
        public void save(byte[] bytes, Book book) {
            try(FileOutputStream fos = new FileOutputStream(getCacheFile(book))) {
                fos.write(bytes);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
