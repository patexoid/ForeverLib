package com.patex.opds.extlib;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.LibException;
import com.patex.entities.*;
import com.patex.messaging.MessengerService;
import com.patex.opds.OPDSEntry;
import com.patex.service.TransactionService;
import com.patex.utils.ExecutorCreator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ExtLibDownloadServiceTest {
    public static final String BOOK_TITLE = "bookTitle";
    private ExtLibConnection connection;

    private ExtLibDownloadService downloadService;
    private String uri = "uri";
    private String type = "typ";
    private ZUser user = new ZUser();
    private SavedBookRepository savedBookRepo;


    @Before
    public void setUp() {
        connection = mock(ExtLibConnection.class);
        ExtLibInScopeRunner scopeRunner = mock(ExtLibInScopeRunner.class);
        when(scopeRunner.runInScope(any(), any(Supplier.class))).
                then(i -> ((Supplier) i.getArguments()[1]).get());
        savedBookRepo = mock(SavedBookRepository.class);
        MessengerService messengerService = mock(MessengerService.class);
        ExecutorCreator executorCreator = mock(ExecutorCreator.class);
        when(executorCreator.createExecutor(any(), any())).thenReturn(MoreExecutors.newDirectExecutorService());
        TransactionService transactionService = new TransactionService();
        downloadService = new
                ExtLibDownloadService(connection, scopeRunner, savedBookRepo, messengerService, executorCreator,
                transactionService);
    }

    @Test
    public void testDownloadBookSuccess() {
        Book book = new Book();
        when(connection.downloadBook(uri, type, user)).thenReturn(book);

        ExtLibrary library = new ExtLibrary();
        Book downloadedBook = downloadService.downloadBook(library, uri, type, user);
        Assert.assertEquals(book, downloadedBook);
        SavedBook savedBook = new SavedBook(library, uri);
        savedBook.success();
        verify(savedBookRepo).save(refEq(savedBook, "id"));
    }

    @Test
    public void testDownloadBookFailed() {
        when(connection.downloadBook(uri, type, user)).thenThrow(new LibException());
        ExtLibrary library = new ExtLibrary();
        try {
            downloadService.downloadBook(library, uri, type, user);
        } catch (LibException e) {
            SavedBook savedBook = new SavedBook(library, uri);
            savedBook.failed();
            verify(savedBookRepo).save(refEq(savedBook, "id"));
            return;
        }
        fail();
    }

    @Test
    public void testDownloadBookFailedTwice() {
        ExtLibrary library = new ExtLibrary();
        SavedBook initialSavedBook = new SavedBook(library, uri);
        initialSavedBook.failed();

        when(connection.downloadBook(uri, type, user)).thenThrow(new LibException());
        when(savedBookRepo.findSavedBooksByExtLibraryAndExtId(library, uri)).thenReturn(Optional.of(initialSavedBook));

        try {
            downloadService.downloadBook(library, uri, type, user);
        } catch (LibException e) {
            SavedBook savedBook = new SavedBook(library, uri);
            savedBook.failed();
            savedBook.failed();
            verify(savedBookRepo).save(refEq(savedBook, "id"));
            return;
        }
        fail();
    }

    @Test
    public void testGetExtLibFeed() {
        ExtLibFeed expectedFeed = new ExtLibFeed("title", Collections.emptyList(), Collections.emptyList());
        when(connection.getFeed(uri)).thenReturn(expectedFeed);

        ExtLibFeed feed = downloadService.getExtLibFeed(new ExtLibrary(), uri);

        assertEquals(expectedFeed, feed);
    }

    @Test
    public void testDownloadAll() throws Exception {
        Book book1 = new Book();
        String bookUri1 = "bookUri1";

        String type = "fb2";
        when(connection.downloadBook(bookUri1, type, user)).thenReturn(book1);
        OPDSEntry entry1 = OPDSEntry.builder("id1", "bookTitle1").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri1, "application/" + type).build();

        Book book2 = new Book();
        String bookUri2 = "bookUri2";
        when(connection.downloadBook(bookUri2, type, user)).thenReturn(book2);
        OPDSEntry entry2 = OPDSEntry.builder("id2", "bookTitle2").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri2, "application/" + type).build();

        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Arrays.asList(entry1, entry2), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(result.isPresent());
        DownloadAllResult books = result.get();
        assertThat(books.getSuccess(), hasSize(2));
        assertEquals(book1, books.getSuccess().get(0));
        assertEquals(book2, books.getSuccess().get(1));
    }

    @Test
    public void testDownloadAllWithAuthor() throws Exception {
        String authorName = "authorName";
        OPDSEntry entry = OPDSEntry.builder("id", BOOK_TITLE).
                addAuthor(authorName, "uri").
                build();
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getAuthors(), hasSize(1));
        assertEquals(authorName, result.getAuthors().get(0));
    }

    @Test
    public void testDownloadAllWithEmptyEntry() throws Exception {
        OPDSEntry entry = OPDSEntry.builder("id", BOOK_TITLE).
                build();
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getEmptyBooks(), hasSize(1));
        assertEquals(BOOK_TITLE, result.getEmptyBooks().get(0).getKey());
    }

    @Test
    public void testDownloadAllWithFail() throws Exception {
        String bookUri = "bookUri";
        String type = "fb2";
        OPDSEntry entry = OPDSEntry.builder("id", BOOK_TITLE).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri, "application/" + type).
                build();
        when(connection.downloadBook(bookUri, type, user)).thenThrow(new LibException());
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getFailed(), hasSize(1));
        assertEquals(BOOK_TITLE, result.getFailed().get(0).getKey());
    }

    @Test
    public void testDownloadAllWithSaved() throws Exception {
        Book book1 = new Book();
        String bookUri1 = "bookUri1";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        when(connection.downloadBook(bookUri1, type, user)).thenReturn(book1);
        OPDSEntry entry1 = OPDSEntry.builder("id1", "bookTitle1").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri1, "application/" + type).build();

        Book book2 = new Book();
        String bookUri2 = "bookUri2";
        when(connection.downloadBook(bookUri2, type, user)).thenReturn(book2);
        OPDSEntry entry2 = OPDSEntry.builder("id2", "bookTitle2").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri2, "application/" + type).build();

        when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(eq(library), eq(Arrays.asList(bookUri1, bookUri2)))).
                thenReturn(Collections.singletonList(new SavedBook(library, bookUri2)));
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Arrays.asList(entry1, entry2), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(library, this.uri, user).get();

        verify(connection).getFeed(uri);
        verify(connection).downloadBook(bookUri1, type, user);
        verifyNoMoreInteractions(connection);

        assertTrue(result.isPresent());
        DownloadAllResult books = result.get();
        assertThat(books.getSuccess(), hasSize(1));
        assertEquals(book1, books.getSuccess().get(0));
    }

    @Test
    public void testDownloadAllWithFailedSavedInfo() throws Exception {
        Book book = new Book();
        String bookUri = "bookUri";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        when(connection.downloadBook(bookUri, type, user)).thenReturn(book);
        OPDSEntry entry1 = OPDSEntry.builder("id", BOOK_TITLE).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri, "application/" + type).build();

        when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(eq(library), eq(Collections.singletonList(bookUri)))).
                thenReturn(Collections.singletonList(new SavedBook(library, bookUri, 1)));
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry1), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(library, this.uri, user).get();

        assertTrue(result.isPresent());
        DownloadAllResult books = result.get();
        assertThat(books.getSuccess(), hasSize(1));
        assertEquals(book, books.getSuccess().get(0));
    }

    @Test
    public void testDownloadAllWithPermanentlyFailedSavedInfo() throws Exception {
        Book book = new Book();
        String bookUri = "bookUri";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        when(connection.downloadBook(bookUri, type, user)).thenReturn(book);
        OPDSEntry entry1 = OPDSEntry.builder("id", BOOK_TITLE).
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri, "application/" + type).build();

        when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(eq(library), eq(Collections.singletonList(bookUri)))).
                thenReturn(Collections.singletonList(new SavedBook(library, bookUri, 100)));
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry1), Collections.emptyList()));

        Optional<DownloadAllResult> result =
                downloadService.downloadAll(library, this.uri, user).get();

        assertFalse(result.isPresent());
    }
}
