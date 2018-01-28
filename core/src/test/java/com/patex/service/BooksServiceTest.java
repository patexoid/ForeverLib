package com.patex.service;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookRepository;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.entities.ZUser;
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
import java.util.List;
import java.util.Optional;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Alexey on 15.07.2017.
 */
public class BooksServiceTest {
    RandomStringGenerator rsg = new RandomStringGenerator.Builder()
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
    /*
    @Test
    public void testFb2Content() throws Exception {
        BookService bookService = new BookService();
        ParserService parserService = new ParserService();
        new ZipFileParser(parserService).register();
        new Fb2FileParser(parserService).register();
        bookService.setParserService(parserService);

        FileInputStream is2 = new FileInputStream("C:\\dev\\projects\\ZombieLib2\\storage\\Aromatov_Obektnyy-podhod_1_Obektnyy-podhod.g6s7Nw.196879.fb2.zip");


        long currtime = System.currentTimeMillis();
        int i = 0;
        for (File f : new File("C:\\dev\\projects\\ZombieLib2\\storage\\").listFiles()) {
            if (f.isFile() && f.getName().toLowerCase().endsWith("fb2.zip")) {
                FileInputStream is = new FileInputStream("C:\\dev\\projects\\ZombieLib2\\storage\\Aromatov_Obektnyy-podhod_1_Obektnyy-podhod.9l9iyg.262326.fb2.zip");
//                boolean shingleComparsion = bookService.shingleComparsion(is, "dsd.fb2.zip", new FileInputStream(f), "dsd.fb2.zip");
                i++;
            }
        }

//        boolean shingleComparsion = bookService.shingleComparsion(is, "dsd.fb2.zip", is2, "dsd.fb2.zip");
        System.out.println("" + i + " " + (System.currentTimeMillis() - currtime));
//        Assert.assertTrue(shingleComparsion);
        ;
    }
    */

    @Before
    public void setUp() {
        bookRepo = mock(BookRepository.class);
        when(bookRepo.findFirstByTitleAndChecksum(any(), any())).thenReturn(Optional.empty());
        when(bookRepo.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);

        sequenceService = mock(SequenceService.class);
        when (sequenceService.mergeSequences(any())).thenAnswer(i -> ((Collection<Sequence>)i.getArguments()[0]).iterator().next());

        authorService = mock(AuthorService.class);
        when(authorService.findFirstByNameIgnoreCase(any())).thenReturn(Optional.empty());

        parserService = mock(ParserService.class);
        fileStorage = mock(StorageService.class);
        transactionService = new TransactionService();
        eventPublisher = mock(ApplicationEventPublisher.class);
        bookService = new BookService(bookRepo, sequenceService, authorService, parserService,
                fileStorage, transactionService,
                eventPublisher);
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
    public void testSequenceReplace(){

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
        savedBook.setSequences(Collections.singletonList(new BookSequence(1, savedSequence,savedBook)));
        savedAuthor.getBooks().add(new AuthorBook(savedAuthor, savedBook));
        when(authorService.findFirstByNameIgnoreCase(existedAuthorName)).thenReturn(Optional.of(savedAuthor));

        Book saved = bookService.uploadBook(fileName, bais, new ZUser());
        assertTrue(saved.getSequences().get(0).getSequence() == savedSequence);
    }

}
