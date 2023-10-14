package com.patex.service;

import com.ibm.icu.text.Transliterator;
import com.patex.entities.AuthorBookEntity;
import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import com.patex.entities.BookEntity;
import com.patex.entities.BookRepository;
import com.patex.entities.BookSequenceEntity;
import com.patex.entities.FileResourceEntity;
import com.patex.entities.GenreEntity;
import com.patex.entities.GenreRepository;
import com.patex.entities.SequenceEntity;
import com.patex.entities.SequenceRepository;
import com.patex.mapper.BookMapper;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import com.patex.zombie.LibException;
import com.patex.zombie.StreamU;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.SimpleBook;
import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.StorageService;
import com.patex.zombie.service.TransactionService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 *
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    private final GenreRepository genreRepository;

    private final SequenceRepository sequenceRepository;
    private final AuthorRepository authorRepository;
    private final ParserService parserService;
    private final StorageService fileStorage;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher publisher;
    private final BookMapper bookMapper;
    private final EntityManager entityManager;
    private final LanguageService languagesService;

    static String[] getFilePath(BookEntity book, String fileName) {
        String[] path = new String[3];
        Transliterator transliterator = Transliterator.getInstance("Any-Latin");
        path[0] = book.getAuthorBooks().stream()
                .map(AuthorBookEntity::getAuthor)
                .map(AuthorEntity::getName)
                .filter(not(String::isBlank))
                .findFirst()
                .map(transliterator::transliterate)
                .map(s -> s.length() > 100 ? s.substring(0, 100) : s)
                .orElse("No Author");
        path[1] = book.getSequences().stream()
                .map(BookSequenceEntity::getSequence)
                .map(SequenceEntity::getName)
                .filter(not(String::isBlank))
                .findFirst()
                .map(transliterator::transliterate)
                .map(s -> s.length() > 100 ? s.substring(0, 100) : s)
                .orElse("No Sequence");
        path[2] = fileName;
        return path;
    }

    @Override
    public Book uploadBook(String fileName, InputStream is, User user) throws LibException {//TODO fix transactions
        return uploadBook(fileName, loadFromStream(is), user);
    }

    public Book uploadBook(String fileName, byte[] bytes, User user) {
        final BookInfo bookInfo = parserService.getBookInfo(fileName, new ByteArrayInputStream(bytes), true);
        byte[] checksum = getChecksum(bytes);
        if (bookRepository.existsByTitleAndChecksum(bookInfo.getBook().getTitle(), checksum)) {
            return transactionService.transactionRequired(() ->
                    bookRepository.findFirstByTitleAndChecksum(bookInfo.getBook().getTitle(), checksum)
                            .map(bookMapper::toDto).get()
            );
        } else {
            String[] filePath = getFilePath(bookInfo.getBook(), fileName);
            String fileId = fileStorage.save(bytes, true, filePath);
            return saveBook(fileName, user, checksum, bookInfo, fileId, bytes.length);
        }
    }

    private synchronized Book saveBook(String fileName, User user, byte[] checksum, BookInfo bookInfo, String fileId, int bookSize) {
        Book result = transactionService.transactionRequired(() -> {
            BookEntity book = bookInfo.getBook();
            Optional<BookEntity> sameBook = bookRepository.findFirstByTitleAndChecksum(book.getTitle(), checksum);
            if (sameBook.isPresent()) {
                return bookMapper.toDto(sameBook.get());
            }
            log.trace("new book:{}", book.getTitle());
            List<AuthorEntity> authors = book.getAuthorBooks().stream().
                    map(AuthorBookEntity::getAuthor).
                    map(author -> authorRepository.findFirstByNameIgnoreCase(author.getName()).orElse(author)).
                    toList();
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
                    collect(Collectors.toMap(Map.Entry::getKey, e -> mergeSequences(e.getValue())));


            book.getGenres().forEach(bg -> {
                Optional<GenreEntity> dbGenre = genreRepository.findByName(bg.getGenre().getName());
                dbGenre.ifPresent(bg::setGenre);
            });

            List<BookSequenceEntity> sequences = book.getSequences().stream().
                    map(bs -> {
                        SequenceEntity sequence = bs.getSequence();
                        return new BookSequenceEntity(bs.getSeqOrder(),
                                sequencesMap.getOrDefault(sequence.getName(), sequence), book);
                    }).collect(Collectors.toList());
            book.setSequences(sequences);
            book.setFileResource(new FileResourceEntity(fileId, "application/fb2+zip", bookSize));//TODO improve me
            book.setFileName(fileName);
            book.setChecksum(checksum);
            book.setCreated(Instant.now());
            getBookLanguage(book).ifPresent(book::setLang);
            BookEntity save = bookRepository.save(book);
            someMagic(book);
            return bookMapper.toDto(save);
        });
        publisher.publishEvent(new BookCreationEvent(result, bookInfo, user));
        return result;
    }

    private Optional<String> getBookLanguage(BookEntity book) {
        return languagesService.detectLang(book::getDescr, () -> getPartialBookContent(book.getFileName(), book.getFileResource().getFilePath()));
    }

    public String getPartialBookContent(String fileName, String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            Iterator<String> contentIterator = parserService.getContentIterator(fileName,
                    fileStorage.load(filePath));
            while (contentIterator.hasNext() && content.length() < 10000) {
                content.append(contentIterator.next()).append("\n");
            }
        } catch (LibException e) {
            log.error(e.getMessage(), e);
        }
        return content.toString();
    }

    private SequenceEntity mergeSequences(List<SequenceEntity> sequences) {
        SequenceEntity main = sequences.get(0);
        if (sequences.size() != 1) {
            sequences.forEach(entityManager::refresh);
            List<BookSequenceEntity> bookSequences = sequences.stream().
                    flatMap(s -> s.getBookSequences().stream()).collect(Collectors.toList());
            bookSequences.forEach(bs -> bs.setSequence(main));
            main.setBookSequences(bookSequences);
            sequenceRepository.save(main);
            sequences.stream().skip(1).forEach(s -> {
                s.setBookSequences(new ArrayList<>());
                sequenceRepository.delete(s);
            });

        }
        return main;
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

    @Override
    public Optional<Book> getBook(long id) {
        return bookRepository.findById(id).map(bookMapper::toDto);
    }

    public Optional<SimpleBook> getSimpleBook(long id) {
        return bookRepository.findById(id).map(bookMapper::toSimpleDto);
    }


    @Override
    public InputStream getBookInputStream(Book book) throws LibException {
        return fileStorage.load(book.getFileResource().getFilePath());
    }

    @Override
    public InputStream getBookCoverInputStream(Book book) throws LibException {
        return fileStorage.load(book.getCover().getFilePath());
    }

    @Override
    public Page<Book> getBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toDto);
    }

    @Override
    @Transactional
    public Book updateBook(Book book) throws LibException {
        return bookRepository.findById(book.getId()).
                map(be -> bookMapper.updateEntity(book, be)).
                map(bookMapper::toDto).
                orElseThrow(() -> new LibException("Book not found"));
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

    @Override
    public Book save(Book book) {
        BookEntity entity = bookMapper.toEntity(book);
        return bookMapper.toDto(bookRepository.save(entity));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Book> getNewBooks(PageRequest pageRequest) {
        return bookRepository.findAllByOrderByCreatedDesc(pageRequest).map(bookMapper::toDto);
    }

    @Override
    public List<SimpleBook> getSameAuthorsBook(SimpleBook primaryBook) {
        return bookRepository.findSameAuthorBook(primaryBook.getId()).
                filter(book -> !book.getId().equals(primaryBook.getId())).
                map(bookMapper::toSimpleDto).
                collect(Collectors.toList());
    }
}
