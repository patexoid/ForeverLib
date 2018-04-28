package com.patex.service;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookRepository;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.entities.ZUser;
import com.patex.parser.BookImage;
import com.patex.parser.BookInfo;
import com.patex.parser.ParserService;
import com.patex.storage.StorageService;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.apache.commons.text.CharacterPredicates.*;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

/**
 * Created by Alexey on 15.07.2017.
 */
public class BooksServiceTest {
    private static final String FILE_NAME = "fileName";
    private static final String FIRST_AUTHOR = "first author";
    private static final String SECOND_AUTHOR = "second author";
    private static final String FIRST_SEQUENCE = "first sequence";

    private final RandomStringGenerator rsg = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(LETTERS, DIGITS)
            .build();
    private BookRepository bookRepo;
    private SequenceService sequenceService;
    private AuthorService authorService;
    private ParserService parserService;
    private StorageService fileStorage;
    private TransactionService transactionService;
    private ApplicationEventPublisher eventPublisher;
    private BookService bookService;
    private ByteArrayInputStream bookIS;
    private ZUser user;
    private BookInfo bookInfo;
    private Book book;

    @Before
    public void setUp() {
        parserService = mock(ParserService.class);
        bookRepo = mock(BookRepository.class);
        sequenceService = mock(SequenceService.class);
        authorService = mock(AuthorService.class);

        bookIS = new ByteArrayInputStream(new byte[0]);
        user = new ZUser();
        bookInfo = new BookInfo();
        book = new Book();
        book.setAuthors(Collections.singleton(new Author(1L, FIRST_AUTHOR)));
        book.setSequences(Collections.singletonList(new BookSequence(1, new Sequence(FIRST_SEQUENCE))));
        bookInfo.setBook(book);

        when(parserService.getBookInfo(eq(FILE_NAME), any())).thenReturn(bookInfo);
        when(bookRepo.findFirstByTitleAndChecksum(any(), any())).thenReturn(Optional.empty());
        when(bookRepo.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);
        when(sequenceService.mergeSequences(any())).thenAnswer(i -> {
            Collection sequences = (Collection) i.getArguments()[0];
            if (sequences == null) {
                return null;
            } else {
                return sequences.iterator().next();
            }
        });
        when(authorService.findFirstByNameIgnoreCase(any())).thenReturn(Optional.empty());

        fileStorage = mock(StorageService.class);
        transactionService = new TransactionService();
        eventPublisher = mock(ApplicationEventPublisher.class);
        bookService = new BookService(bookRepo, sequenceService, authorService, parserService,
                fileStorage, transactionService,
                eventPublisher);
    }

    @Test
    public void verifyUploadBook() {
        Book result = bookService.uploadBook(FILE_NAME, bookIS, user);
        verify(bookRepo).save(this.book);
        verify(fileStorage).save(any(), eq(FILE_NAME));
        assertEquals(FILE_NAME, book.getFileName());
        assertEquals(result, book);
    }

    @Test
    public void verifyUploadBookWithSavedAuthor() {
        long authorID = 42;
        when(authorService.findFirstByNameIgnoreCase(FIRST_AUTHOR)).thenReturn(Optional.of(new Author(authorID, FIRST_AUTHOR)));

        Book result = bookService.uploadBook(FILE_NAME, bookIS, user);

        Author resultAuthor = result.getAuthorBooks().get(0).getAuthor();
        assertEquals(authorID, resultAuthor.getId().longValue());
    }


    @Test
    public void verifyUploadBookWithSavedAuthorAndSequence() {
        long authorID = 42;
        long seqeunceId = 54;
        Author savedAuthor = new Author(authorID, FIRST_AUTHOR);
        Book savedBook = new Book();
        Sequence sequence = new Sequence(seqeunceId, FIRST_SEQUENCE);
        savedBook.setSequences(Collections.singletonList(new BookSequence(1, sequence, savedBook)));
        savedAuthor.setBooks(Collections.singletonList(new AuthorBook(savedAuthor, savedBook)));
        when(authorService.findFirstByNameIgnoreCase(FIRST_AUTHOR)).thenReturn(Optional.of(savedAuthor));

        Book result = bookService.uploadBook(FILE_NAME, bookIS, user);

        Author resultAuthor = result.getAuthorBooks().get(0).getAuthor();
        assertEquals(authorID, resultAuthor.getId().longValue());
        Sequence resultSequnce = result.getSequences().get(0).getSequence();
        assertEquals(seqeunceId, resultSequnce.getId().longValue());
    }


    @Test
    public void verifyMergeSequenceDuringBookUpload() {

        long firstSavedSequenceId = 1L;
        long secondSavedSequenceId = 2L;
        long mergedSequenceId = 3L;
        Author firstSavedAuthor = new Author(FIRST_AUTHOR);
        Book firstSavedBook = new Book();
        Sequence firstSequence = new Sequence(firstSavedSequenceId, FIRST_SEQUENCE);
        firstSavedBook.setSequences(Collections.singletonList(new BookSequence(1, firstSequence, firstSavedBook)));
        firstSavedAuthor.setBooks(Collections.singletonList(new AuthorBook(firstSavedAuthor, firstSavedBook)));
        when(authorService.findFirstByNameIgnoreCase(FIRST_AUTHOR)).thenReturn(Optional.of(firstSavedAuthor));

        Author secondSavedAuthor = new Author(SECOND_AUTHOR);
        Book secondSavedBook = new Book();
        Sequence secondSequence = new Sequence(secondSavedSequenceId, FIRST_SEQUENCE);
        secondSavedBook.setSequences(Collections.singletonList(new BookSequence(1, secondSequence, secondSavedBook)));
        secondSavedAuthor.setBooks(Collections.singletonList(new AuthorBook(secondSavedAuthor, secondSavedBook)));
        when(authorService.findFirstByNameIgnoreCase(SECOND_AUTHOR)).thenReturn(Optional.of(secondSavedAuthor));

        book.setAuthors(Arrays.asList(new Author(FIRST_AUTHOR), new Author(SECOND_AUTHOR)));

        when(sequenceService.mergeSequences(any())).thenReturn(new Sequence(mergedSequenceId, FIRST_SEQUENCE));

        Book result = bookService.uploadBook(FILE_NAME, bookIS, user);

        verify(sequenceService).mergeSequences(anyList());
        assertEquals(mergedSequenceId, result.getSequences().get(0).getSequence().getId().longValue());
    }


    @Test
    public void verifyBookCoverSave() {
        BookImage bookImage = new BookImage();
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5, 6};
        bookImage.setImage(imageBytes);
        String extension = "extension";
        bookImage.setType("image/" + extension);
        bookInfo.setBookImage(bookImage);

        bookService.uploadBook(FILE_NAME, bookIS, user);
        verify(fileStorage).save(aryEq(imageBytes), eq("image"), eq(FILE_NAME + "." + extension));

    }


    @Test
    public void testSameBookUpload() {
        ZUser user = new ZUser();
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        String fileName = rsg.generate(10);
        String uploadedTitle = rsg.generate(10);
        String existedTitle = rsg.generate(10);
        BookInfo uploadedBookInfo = new BookInfo();
        Book uploadedBook = new Book();
        uploadedBook.setTitle(uploadedTitle);
        uploadedBookInfo.setBook(uploadedBook);
        when(parserService.getBookInfo(eq(fileName), any(InputStream.class))).thenReturn(uploadedBookInfo);
        Book savedBook = new Book();
        savedBook.setTitle(existedTitle);
        when(bookRepo.findFirstByTitleAndChecksum(eq(uploadedTitle), any())).thenReturn(Optional.of(savedBook));

        Book book = bookService.uploadBook(fileName, bais, user);
        assertEquals("should be saved book", existedTitle, book.getTitle());
    }

    @Test
    public void testSavedAuthorReplace() {
        String newAuthorName = rsg.generate(10);
        String existedAuthorName = rsg.generate(10);
        String fileName = rsg.generate(10);
        Book book = new Book();
        AuthorBook abWithNewAuthor = new AuthorBook();
        Author newAuthor = new Author();
        newAuthor.setName(newAuthorName);
        abWithNewAuthor.setAuthor(newAuthor);
        AuthorBook abWithExistedAuthor = new AuthorBook();
        Author existedAuthor = new Author();
        existedAuthor.setName(existedAuthorName);
        abWithExistedAuthor.setAuthor(existedAuthor);
        book.setAuthorBooks(Arrays.asList(abWithNewAuthor, abWithExistedAuthor));
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBook(book);
        when(parserService.getBookInfo(eq(fileName), any())).thenReturn(bookInfo);
        Author savedAuthor = new Author();
        savedAuthor.setName(existedAuthorName);
        when(authorService.findFirstByNameIgnoreCase(existedAuthorName)).thenReturn(Optional.of(savedAuthor));

        Book saved = bookService.uploadBook(fileName, bais, new ZUser());
        assertTrue(saved.getAuthorBooks().get(0).getAuthor() == newAuthor);
        assertTrue(saved.getAuthorBooks().get(1).getAuthor() == savedAuthor);
    }

    @Test
    public void testSequenceReplace() {

        String sequenceName = rsg.generate(10);
        String existedAuthorName = rsg.generate(10);
        String fileName = rsg.generate(10);
        long savedSeqId = RandomUtils.nextLong();

        Book book = new Book();

        Sequence newSequence = new Sequence();
        newSequence.setName(sequenceName);
        book.setSequences(Collections.singletonList(new BookSequence(2, newSequence)));
        AuthorBook abWithExistedAuthor = new AuthorBook();
        Author existedAuthor = new Author();
        existedAuthor.setName(existedAuthorName);
        abWithExistedAuthor.setAuthor(existedAuthor);
        book.setAuthorBooks(Collections.singletonList(abWithExistedAuthor));
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBook(book);
        when(parserService.getBookInfo(eq(fileName), any())).thenReturn(bookInfo);
        Author savedAuthor = new Author();
        savedAuthor.setName(existedAuthorName);
        Book savedBook = new Book();
        Sequence savedSequence = new Sequence();
        savedSequence.setName(sequenceName);
        savedSequence.setId(savedSeqId);
        savedBook.setSequences(Collections.singletonList(new BookSequence(1, savedSequence, savedBook)));
        savedAuthor.getBooks().add(new AuthorBook(savedAuthor, savedBook));
        when(authorService.findFirstByNameIgnoreCase(existedAuthorName)).thenReturn(Optional.of(savedAuthor));

        Book saved = bookService.uploadBook(fileName, bais, new ZUser());
        assertTrue(saved.getSequences().get(0).getSequence() == savedSequence);
    }
}