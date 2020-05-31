package com.patex.service;

import com.patex.entities.AuthorBookEntity;
import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import com.patex.entities.BookEntity;
import com.patex.entities.BookRepository;
import com.patex.entities.BookSequenceEntity;
import com.patex.entities.FileResourceEntity;
import com.patex.entities.SequenceEntity;
import com.patex.entities.SequenceRepository;
import com.patex.mapper.BookMapper;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import com.patex.zombie.service.StorageService;
import com.patex.zombie.LibException;
import com.patex.zombie.StreamU;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookImage;
import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;
    private final SequenceRepository sequenceRepository;
    private final AuthorRepository authorRepository;
    private final ParserService parserService;
    private final StorageService fileStorage;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher publisher;
    private final BookMapper bookMapper;
    private final EntityManager entityManager;


    @Override
    public synchronized Book uploadBook(String fileName, InputStream is, User user) throws LibException {//TODO fix transactions

        Book result =  transactionService.transactionRequired(() -> {
            byte[] byteArray = loadFromStream(is);
            byte[] checksum = getChecksum(byteArray);
            BookInfo bookInfo = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));
            BookEntity book = bookInfo.getBook();
            Optional<BookEntity> sameBook = bookRepository.findFirstByTitleAndChecksum(book.getTitle(), checksum);
            if (sameBook.isPresent()) {
                return bookMapper.toDto(sameBook.get());
            }
            log.trace("new book:{}", book.getFileName());
            List<AuthorEntity> authors = book.getAuthorBooks().stream().
                    map(AuthorBookEntity::getAuthor).
                    map(author -> authorRepository.findFirstByNameIgnoreCase(author.getName()).orElse(author)).
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
                    collect(Collectors.toMap(Map.Entry::getKey, e -> mergeSequences(e.getValue())));

            List<BookSequenceEntity> sequences = book.getSequences().stream().
                    map(bs -> {
                        SequenceEntity sequence = bs.getSequence();
                        return new BookSequenceEntity(bs.getSeqOrder(),
                                sequencesMap.getOrDefault(sequence.getName(), sequence), book);
                    }).collect(Collectors.toList());
            book.setSequences(sequences);

            String fileId = fileStorage.save(byteArray, fileName);
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
            return bookMapper.toDto(save);
        });
        publisher.publishEvent(new BookCreationEvent(result, user));
        return result;
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
    public String saveCover(String fileName, BookImage bookImage) {
        String coverName = fileName;
        String[] type = bookImage.getType().split("/");
        if (type.length > 1) {
            coverName = fileName + "." + type[1];
        }
        return fileStorage.save(bookImage.getImage(), "image", coverName);
    }

    @Override
    public Book save(Book book) {
        BookEntity entity = bookMapper.toEntity(book);
        return bookMapper.toDto(bookRepository.save(entity));
    }

    @Override
    public Stream<Book> findAll() {
        return bookRepository.findAll().map(bookMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getNewBooks(PageRequest pageRequest) {
        return bookRepository.findAllByOrderByCreatedDesc(pageRequest).map(bookMapper::toDto);
    }

    @Override
    public List<Book> getSameAuthorsBook(Book primaryBook) {
        return bookRepository.findById(primaryBook.getId()).stream().
                map(BookEntity::getAuthorBooks).flatMap(Collection::stream).
                map(AuthorBookEntity::getAuthor).
                map(AuthorEntity::getBooks).flatMap(Collection::stream).
                map(AuthorBookEntity::getBook).
                filter(book -> !book.getId().equals(primaryBook.getId())).
                filter(StreamU.distinctByKey(BookEntity::getId)).
                map(bookMapper::toDto).
                collect(Collectors.toList());
    }
}
