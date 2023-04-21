package com.patex.service;


import com.patex.messaging.MessengerService;
import com.patex.model.CheckDuplicateMessage;
import com.patex.parser.ParserService;
import com.patex.shingle.ShingleCacheStorage;
import com.patex.shingle.ShingleMatcher;
import com.patex.shingle.ShingleSearch;
import com.patex.shingle.Shingleable;
import com.patex.zombie.LibException;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookAuthor;
import com.patex.zombie.model.BookSequence;
import com.patex.zombie.model.Res;
import com.patex.zombie.model.SimpleBook;
import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.StorageService;
import com.patex.zombie.service.UserService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DuplicateHandler {

    private static final Logger log = LoggerFactory.getLogger(DuplicateHandler.class);
    private final BookService bookService;
    private final MessengerService messenger;
    private final StorageService fileStorage;
    private final ParserService parserService;
    private final ShingleSearch<SimpleBook, Long> shingleSearch;
    private final UserService userService;

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public DuplicateHandler(BookService bookService, MessengerService messenger, StorageService fileStorage,
                            ParserService parserService,
                            UserService userService,
                            @Value("${duplicateCheck.shingleCoeff:1}") int coef,
                            @Value("${duplicateCheck.fastCacheSize:100}") int cacheSize,
                            @Value("${duplicateCheck.storageCacheFolder:}") String storageFolder) {
        this.userService = userService;
        this.bookService = bookService;
        this.messenger = messenger;
        this.fileStorage = fileStorage;
        this.parserService = parserService;
        ShingleMatcher.Builder<Long, SimpleBook> shingleMatherBuilder =
                ShingleMatcher.builder(ShingleableBook::new, SimpleBook::getId).
                        byteArraySize(16).
                        cache(cacheSize, 1, TimeUnit.HOURS).
                        coef(coef).
                        hashAlgorithm("MD5").
                        shingleSize(10).
                        similarity(0.7f);
        if (StringUtils.isNotEmpty(storageFolder)) {
            shingleMatherBuilder = shingleMatherBuilder.storage(new BookShingleCacheStorage(storageFolder));
        }
        ShingleMatcher<SimpleBook, Long> shingleMatcher = shingleMatherBuilder.build();
        Function<SimpleBook, Collection<SimpleBook>> getSameAuthorsBook = this::getSameAuthorsBook;
        shingleSearch = ShingleSearch.<SimpleBook, Long>builder().preSearch(getSameAuthorsBook).shingleMatcher(shingleMatcher).build();
    }

    private List<SimpleBook> getSameAuthorsBook(SimpleBook primaryBook) {
        return bookService.getSameAuthorsBook(primaryBook).stream().
                sorted(Comparator.comparing(
                        book -> levenshteinDistance.apply(book.getTitle(), primaryBook.getTitle()))).
                collect(Collectors.toList());
    }

    public void checkForDuplicate(CheckDuplicateMessage bookCheckQueue) {
        SimpleBook primary = bookService.getSimpleBook(bookCheckQueue.book()).get();
        User user = userService.getUser(bookCheckQueue.user());
        try {
            shingleSearch.findSimilar(primary).
                    ifPresent(book -> markDuplications(primary, book, user));
        } catch (Exception e) {
            log.error("Duplication check exception book " +
                    " book.id= " + primary.getId() +
                    " book.title = " + primary.getTitle() +
                    " book.filename" + primary.getFileName() +
                    " exception=" + e.getMessage());
            log.trace("", e);
        }
    }

    private void markDuplications(SimpleBook first, SimpleBook second, User user) {
        try {

            Long primaryId, secondaryId;
            if (first.getContentSize() > second.getContentSize()) {
                primaryId = first.getId();
                secondaryId = second.getId();
            } else if (first.getContentSize() < second.getContentSize()) {
                primaryId = second.getId();
                secondaryId = first.getId();
            } else if(first.getCreated().isBefore(second.getCreated())){
                primaryId = first.getId();
                secondaryId = second.getId();
            } else {
                primaryId = second.getId();
                secondaryId = first.getId();
            }
            Book primary=bookService.getBook(primaryId).get();
            Book secondary = bookService.getBook(secondaryId).get();

            List<Long> sequences = primary.getSequences().stream().
                    map(BookSequence::getId).
                    toList();
            List<Long> authors = primary.getAuthors().stream().
                    map(BookAuthor::getId).
                    toList();

            secondary.setDuplicate(true);
            this.bookService.updateBook(secondary);
            shingleSearch.invalidate(secondary);


            for (BookAuthor author : secondary.getAuthors()) {
                if (!authors.contains(author.getId())) {
                    primary.getAuthors().add(new BookAuthor(author.getId(), author.getName()));
                }
            }
            for (BookSequence sequence : secondary.getSequences()) {
                if (!sequences.contains(sequence.getId())) {
                    primary.getSequences().
                            add(new BookSequence(sequence.getId(), sequence.getSeqOrder(), sequence.getSequenceName()));
                }
            }
            String message = "Book:" + first.getTitle() + "\nPrimary: " + primary.getTitle() + "\nDuplicate: " +
                    secondary.getTitle();
            log.info(message);
            if (user != null) {
                Res messageRes = new Res("duplicate.check.result", first.getTitle(),
                        primary.getTitle(), secondary.getTitle());
                messenger.sendMessageToUser(messageRes, user.getUsername());
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

    private static class BookShingleCacheStorage implements ShingleCacheStorage<Long> {
        private final String storageFolder;

        BookShingleCacheStorage(String storageFolder) {
            this.storageFolder = storageFolder;
            File dir = new File(storageFolder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        @Override
        @SneakyThrows
        public InputStream load(Long bookId) {
            File cache = getCacheFile(bookId);
            if (cache.exists()) {
                return new BufferedInputStream(new FileInputStream(cache),1024*1024);
            }
            return null;
        }

        private File getCacheFile(Long bookId) {
            String pathname = storageFolder + File.separator + String.valueOf(bookId).chars().
                    boxed().map(String::valueOf).collect(Collectors.joining(File.separator)) + ".dh";
            return new File(pathname);
        }

        @Override
        public void save(Long bookId, byte[] bytes) {
            File cacheFile = getCacheFile(bookId);
            File parentFile = cacheFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(bytes);
                fos.flush();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private class ShingleableBook implements Shingleable {
        private final SimpleBook book;
        private Iterator<String> contentIterator;
        private InputStream is;

        ShingleableBook(SimpleBook book) {
            this.book = book;
            is = fileStorage.load(book.getFileResource().getFilePath());
            this.contentIterator = parserService.getContentIterator(book.getFileName(),
                    new BufferedInputStream(is, 1024 * 1024));
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
