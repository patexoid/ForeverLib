package com.patex.forever.opds;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.forever.messaging.MessengerService;
import com.patex.forever.LibException;
import com.patex.forever.model.Book;
import com.patex.forever.model.Res;
import com.patex.forever.opds.entity.ExtLibrary;
import com.patex.forever.opds.entity.SavedBook;
import com.patex.forever.opds.entity.SavedBookRepository;
import com.patex.forever.opds.model.DownloadAllResult;
import com.patex.forever.opds.model.ExtLibFeed;
import com.patex.forever.opds.model.OPDSEntryBuilder;
import com.patex.forever.opds.model.OPDSEntry;
import com.patex.forever.opds.service.ExtLibConnection;
import com.patex.forever.opds.service.ExtLibDownloadService;
import com.patex.forever.opds.service.ExtLibInScopeRunner;
import com.patex.forever.opds.service.ExtLibService;
import com.patex.forever.service.ExecutorCreator;
import com.patex.forever.service.TransactionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unchecked")
@Disabled
public class ExtLibDownloadServiceTest {
    public static final String BOOK_TITLE = "bookTitle";
    private ExtLibConnection connection;

    private ExtLibDownloadService downloadService;
    private String uri = "uri";
    private String type = "typ";
    private String user = "user";
    private SavedBookRepository savedBookRepo;


    @BeforeEach
    public void setUp() {
        connection = Mockito.mock(ExtLibConnection.class);
        ExtLibInScopeRunner scopeRunner = Mockito.mock(ExtLibInScopeRunner.class);
        Mockito.when(scopeRunner.runInScope(ArgumentMatchers.any(), ArgumentMatchers.any(Supplier.class))).
                then(i -> ((Supplier) i.getArguments()[1]).get());
        savedBookRepo = Mockito.mock(SavedBookRepository.class);
        MessengerService messengerService = Mockito.mock(MessengerService.class);
        ExecutorCreator executorCreator = Mockito.mock(ExecutorCreator.class);
        Mockito.when(executorCreator.createExecutor(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(MoreExecutors.newDirectExecutorService());
        TransactionService transactionService = new TransactionService();
        downloadService = new
                ExtLibDownloadService(connection, scopeRunner, savedBookRepo, messengerService, executorCreator,
                transactionService);
    }

    @Test
    public void testDownloadBookSuccess() {
        Book book = new Book();
        Mockito.when(connection.downloadBook(uri, type, user)).thenReturn(book);

        ExtLibrary library = new ExtLibrary();
        Book downloadedBook = downloadService.downloadBook(library, uri, type, user);
        assertEquals(book, downloadedBook);
        SavedBook savedBook = new SavedBook(library, uri);
        savedBook.success();
        Mockito.verify(savedBookRepo).save(ArgumentMatchers.refEq(savedBook, "id"));
    }

    @Test
    public void testDownloadBookFailed() {
        Mockito.when(connection.downloadBook(uri, type, user)).thenThrow(new LibException());
        ExtLibrary library = new ExtLibrary();
        try {
            downloadService.downloadBook(library, uri, type, user);
        } catch (LibException e) {
            SavedBook savedBook = new SavedBook(library, uri);
            savedBook.failed();
            Mockito.verify(savedBookRepo).save(ArgumentMatchers.refEq(savedBook, "id"));
            return;
        }
        fail();
    }

    @Test
    public void testDownloadBookFailedTwice() {
        ExtLibrary library = new ExtLibrary();
        SavedBook initialSavedBook = new SavedBook(library, uri);
        initialSavedBook.failed();

        Mockito.when(connection.downloadBook(uri, type, user)).thenThrow(new LibException());
        Mockito.when(savedBookRepo.findSavedBooksByExtLibraryAndExtId(library, uri)).thenReturn(Optional.of(initialSavedBook));

        try {
            downloadService.downloadBook(library, uri, type, user);
        } catch (LibException e) {
            SavedBook savedBook = new SavedBook(library, uri);
            savedBook.failed();
            savedBook.failed();
            Mockito.verify(savedBookRepo).save(ArgumentMatchers.refEq(savedBook, "id"));
            return;
        }
        fail();
    }

    @Test
    public void testGetExtLibFeed() {
        ExtLibFeed expectedFeed = new ExtLibFeed("title", Collections.emptyList(), Collections.emptyList());
        Mockito.when(connection.getFeed(uri)).thenReturn(expectedFeed);

        ExtLibFeed feed = downloadService.getExtLibFeed(new ExtLibrary(), uri);

        assertEquals(expectedFeed, feed);
    }

    @Test
    public void testDownloadAll() throws Exception {
        Book book1 = new Book();
        String bookUri1 = "bookUri1";

        String type = "fb2";
        Mockito.when(connection.downloadBook(bookUri1, type, user)).thenReturn(book1);
        final Res bookTitle1 = new Res("bookTitle1");
        OPDSEntry entry1 = new OPDSEntryBuilder("id1", Instant.now(), bookTitle1).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri1, "application/" + type).build();

        Book book2 = new Book();
        String bookUri2 = "bookUri2";
        Mockito.when(connection.downloadBook(bookUri2, type, user)).thenReturn(book2);
        final Res bookTitle2 = new Res("bookTitle2");
        OPDSEntry entry2 = new OPDSEntryBuilder("id2", Instant.now(), bookTitle2).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri2, "application/" + type).build();

        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Arrays.asList(entry1, entry2), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(result.isPresent());
        DownloadAllResult books = result.get();
        assertThat(books.getSuccess(), Matchers.hasSize(2));
        assertEquals(book1, books.getSuccess().get(0));
        assertEquals(book2, books.getSuccess().get(1));
    }

    @Test
    public void testDownloadAllWithAuthor() throws Exception {
        String authorName = "authorName";
        final Res res = new Res(BOOK_TITLE);
        OPDSEntry entry = new OPDSEntryBuilder("id", Instant.now(), res).
                addAuthor(authorName, "uri").
                build();
        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getAuthors(), Matchers.hasSize(1));
        assertEquals(authorName, result.getAuthors().get(0));
    }

    @Test
    public void testDownloadAllWithEmptyEntry() throws Exception {
        Res title = new Res(BOOK_TITLE);
        OPDSEntry entry = new OPDSEntryBuilder("id", Instant.now(), title).
                build();
        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getEmptyBooks(), Matchers.hasSize(1));
        assertEquals(title, result.getEmptyBooks().get(0));
    }

    @Test
    public void testDownloadAllWithFail() throws Exception {
        Res title = new Res(BOOK_TITLE);
        String bookUri = "bookUri";
        String type = "fb2";
        OPDSEntry entry = new OPDSEntryBuilder("id", Instant.now(), title).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri, "application/" + type).
                build();
        Mockito.when(connection.downloadBook(bookUri, type, user)).thenThrow(new LibException());
        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getFailed(), Matchers.hasSize(1));
        assertEquals(title, result.getFailed().get(0));
    }

    @Test
    public void testDownloadAllWithSaved() throws Exception {
        Book book1 = new Book();
        String bookUri1 = "bookUri1";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        Mockito.when(connection.downloadBook(bookUri1, type, user)).thenReturn(book1);
        final Res bookTitle1 = new Res("bookTitle1");
        OPDSEntry entry1 = new OPDSEntryBuilder("id1", Instant.now(), bookTitle1).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri1, "application/" + type).build();

        Book book2 = new Book();
        String bookUri2 = "bookUri2";
        Mockito.when(connection.downloadBook(bookUri2, type, user)).thenReturn(book2);
        final Res bookTitle2 = new Res("bookTitle2");
        OPDSEntry entry2 = new OPDSEntryBuilder("id2", Instant.now(), bookTitle2).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri2, "application/" + type).build();

        Mockito.when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(ArgumentMatchers.eq(library), ArgumentMatchers.eq(Arrays.asList(bookUri1, bookUri2)))).
                thenReturn(Collections.singletonList(new SavedBook(library, bookUri2)));
        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Arrays.asList(entry1, entry2), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(library, this.uri, user).get();

        Mockito.verify(connection).getFeed(uri);
        Mockito.verify(connection).downloadBook(bookUri1, type, user);
        verifyNoMoreInteractions(connection);

        assertTrue(result.isPresent());
        DownloadAllResult books = result.get();
        assertThat(books.getSuccess(), Matchers.hasSize(1));
        assertEquals(book1, books.getSuccess().get(0));
    }

    @Test
    public void testDownloadAllWithFailedSavedInfo() throws Exception {
        Book book = new Book();
        String bookUri = "bookUri";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        Mockito.when(connection.downloadBook(bookUri, type, user)).thenReturn(book);
        final Res bookTitle = new Res("bookTitle");
        OPDSEntry entry1 = new OPDSEntryBuilder("id", Instant.now(), bookTitle).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri, "application/" + type).build();

        Mockito.when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(ArgumentMatchers.eq(library), ArgumentMatchers.eq(Collections.singletonList(bookUri)))).
                thenReturn(Collections.singletonList(new SavedBook(library, bookUri, 1)));
        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry1), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(library, this.uri, user).get();

        assertTrue(result.isPresent());
        DownloadAllResult books = result.get();
        assertThat(books.getSuccess(), Matchers.hasSize(1));
        assertEquals(book, books.getSuccess().get(0));
    }

    @Test
    public void testDownloadAllWithPermanentlyFailedSavedInfo() throws Exception {
        Book book = new Book();
        String bookUri = "bookUri";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        Mockito.when(connection.downloadBook(bookUri, type, user)).thenReturn(book);
        final Res bookTitle = new Res("bookTitle");
        OPDSEntry entry1 = new OPDSEntryBuilder("id", Instant.now(), bookTitle).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri, "application/" + type).build();

        Mockito.when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(ArgumentMatchers.eq(library), ArgumentMatchers.eq(Collections.singletonList(bookUri)))).
                thenReturn(Collections.singletonList(new SavedBook(library, bookUri, 100)));
        Mockito.when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry1), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(library, this.uri, user).get();

        assertFalse(result.isPresent());
    }
}
