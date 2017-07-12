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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 *
 *
 */
@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ParserService parserService;

    @Autowired
    private StorageService fileStorage;

    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public synchronized Book uploadBook(String fileName, InputStream is) throws LibException {
        byte[] byteArray = loadFromStream(is);
        byte[] checksum = getChecksum(byteArray);
        Book book = parserService.getBookInfo(fileName, new ByteArrayInputStream(byteArray));
        Optional<Book> sameBook = bookRepository.findByTitleIgnoreCase(book.getTitle()).
                stream().filter(loaded -> Arrays.equals(checksum, loaded.getChecksum())).
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
        book.setSize(byteArray.length);
        book.setChecksum(checksum);
        Book save = bookRepository.save(book);
        book.getAuthorBooks().stream().
                filter(authorBook -> !authorBook.getAuthor().getBooks().contains(authorBook)).
                forEach(authorBook -> authorBook.getAuthor().getBooks().add(authorBook));
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

}
