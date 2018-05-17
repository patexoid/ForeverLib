package com.patex.extlib;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.SavedBook;
import com.patex.entities.SavedBookRepository;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.opds.OPDSEntryBuilder;
import com.patex.opds.converters.OPDSEntry;
import com.patex.utils.ExecutorCreator;
import com.patex.utils.Res;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
        downloadService = new
                ExtLibDownloadService(connection, scopeRunner, savedBookRepo, messengerService, executorCreator);
    }

    @Test
    public void testDownloadBook() {
        Book book = new Book();
        when(connection.downloadBook(uri, type, user)).thenReturn(book);

        Book downloadedBook = downloadService.downloadBook(new ExtLibrary(), uri, type, user);
        assertEquals(book, downloadedBook);
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
        OPDSEntry entry1 = new OPDSEntryBuilder("id1", new Date(), "bookTitle1").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri1, "application/" + type).build();

        Book book2 = new Book();
        String bookUri2 = "bookUri2";
        when(connection.downloadBook(bookUri2, type, user)).thenReturn(book2);
        OPDSEntry entry2 = new OPDSEntryBuilder("id2", new Date(), "bookTitle2").
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
        OPDSEntry entry = new OPDSEntryBuilder("id", new Date(), BOOK_TITLE).
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
        Res title = new Res(BOOK_TITLE);
        OPDSEntry entry = new OPDSEntryBuilder("id", new Date(), title).
                build();
        when(connection.getFeed(this.uri)).
                thenReturn(new ExtLibFeed("title", Collections.singletonList(entry), Collections.emptyList()));

        Optional<DownloadAllResult> resultO =
                downloadService.downloadAll(new ExtLibrary(), this.uri, user).get();

        assertTrue(resultO.isPresent());
        DownloadAllResult result = resultO.get();
        assertThat(result.getEmptyBooks(), hasSize(1));
        assertEquals(title, result.getEmptyBooks().get(0));
    }

    @Test
    public void testDownloadAllWithFail() throws Exception {
        Res title = new Res(BOOK_TITLE);
        String bookUri = "bookUri";
        String type = "fb2";
        OPDSEntry entry = new OPDSEntryBuilder("id", new Date(), title).
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
        assertEquals(title, result.getFailed().get(0));
    }

    @Test
    public void testDownloadAllWithSaved() throws Exception {
        Book book1 = new Book();
        String bookUri1 = "bookUri1";
        ExtLibrary library = new ExtLibrary();
        String type = "fb2";

        when(connection.downloadBook(bookUri1, type, user)).thenReturn(book1);
        OPDSEntry entry1 = new OPDSEntryBuilder("id1", new Date(), "bookTitle1").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri1, "application/" + type).build();

        Book book2 = new Book();
        String bookUri2 = "bookUri2";
        when(connection.downloadBook(bookUri2, type, user)).thenReturn(book2);
        OPDSEntry entry2 = new OPDSEntryBuilder("id2", new Date(), "bookTitle2").
                addLink(ExtLibService.REQUEST_P_NAME + "=" + bookUri2, "application/" + type).build();

        when(savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(library, Arrays.asList(bookUri1, bookUri2))).
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
}
