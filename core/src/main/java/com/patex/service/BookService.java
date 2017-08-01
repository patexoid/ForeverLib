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
import com.patex.parser.ParserService;
import com.patex.storage.StorageService;
import com.patex.utils.StreamU;
import com.patex.utils.shingle.ShingleComparsion;
import com.patex.utils.shingle.Shingleable;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    @Autowired
    public BookService(BookRepository bookRepository, SequenceService sequenceService,
                       AuthorService authorService, ParserService parserService, StorageService fileStorage,
                       BookCheckQueueRepository bookCheckQueueRepo) {
        this.bookRepository = bookRepository;
        this.sequenceService = sequenceService;
        this.authorService = authorService;
        this.parserService = parserService;
        this.fileStorage = fileStorage;
        this.bookCheckQueueRepo = bookCheckQueueRepo;
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
            sameBook.get();
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
        book.setSize(byteArray.length);
        book.setChecksum(checksum);
        Book save = bookRepository.save(book);
        book.getAuthorBooks().stream().
                filter(authorBook -> !authorBook.getAuthor().getBooks().contains(authorBook)).
                forEach(authorBook -> authorBook.getAuthor().getBooks().add(authorBook));
        bookCheckQueueRepo.save(new BookCheckQueue(book));
        executor.execute(this::checkForDuplicate);
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
    public void checkForDuplicateSecured() {
        checkForDuplicate();
    }

    private synchronized void checkForDuplicate() {
        Iterable<BookCheckQueue> queue = bookCheckQueueRepo.findAll();
        for (BookCheckQueue bookCheckQueue : queue) {
            checkForDuplicate(bookCheckQueue);
        }

    }

    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    private void checkForDuplicate(BookCheckQueue bookCheckQueue) {
        try {
            Book checkedBook = bookRepository.findOne(bookCheckQueue.getBook().getId());
            if (!checkedBook.isDuplicate()) {
                Set<Book> duplicates = findDuplications(checkedBook);
                if (!duplicates.isEmpty()) {
                    markDuplications(checkedBook, duplicates);
                }
            }
            bookCheckQueueRepo.delete(bookCheckQueue);
        } catch (Exception e) {
            log.error("Duplication check exception book id= " + bookCheckQueue.getBook().getId() +
                    " title = " + bookCheckQueue.getBook().getTitle() + " exception=" + e.getMessage(), e);
        }
    }

    private void markDuplications(Book checkedBook, Set<Book> duplicates) {
        duplicates.add(checkedBook);
        Book primaryBook = duplicates.stream().sorted(Comparator.comparingInt(Book::getSize)).findFirst().get();

        List<Long> sequences = primaryBook.getSequences().stream().
                map(BookSequence::getSequence).
                map(Sequence::getId).
                collect(Collectors.toList());
        List<Long> authors = primaryBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(Author::getId).
                collect(Collectors.toList());
        duplicates.remove(primaryBook);
        for (Book duplicate : duplicates) {
            duplicate.setDuplicate(true);
            this.bookRepository.save(duplicate);
            for (AuthorBook authorBook : duplicate.getAuthorBooks()) {
                Author author = authorBook.getAuthor();
                if (!authors.contains(author.getId())) {
                    primaryBook.getAuthorBooks().add(new AuthorBook(author, primaryBook));
                }
            }
            for (BookSequence bookSequence : duplicate.getSequences()) {
                Sequence sequence = bookSequence.getSequence();
                if (!sequences.contains(sequence.getId())) {
                    primaryBook.getSequences().
                            add(new BookSequence(bookSequence.getSeqOrder(), sequence, primaryBook));
                }
            }
        }
        this.bookRepository.save(primaryBook);
    }

    private Set<Book> findDuplications(Book checkedBook) {
        List<Book> sameAuthorBooks = checkedBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).
                flatMap(a -> a.getBooks().stream().map(AuthorBook::getBook)).
                filter(book -> !book.getId().equals(checkedBook.getId())).
                filter(book -> !book.isDuplicate()).
                collect(Collectors.toList());

        return new ShingleComparsion().findSimilar(checkedBook, sameAuthorBooks, ShingleableBook::new
        );
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
            return book.getSize();
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
