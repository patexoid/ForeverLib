package com.patex.extlib;

import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ExtLibraryRepository;
import com.patex.entities.Subscription;
import com.patex.entities.ZUser;
import com.patex.opds.OPDSEntryBuilder;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSLink;
import com.patex.service.ZUserService;
import com.patex.utils.ExecutorCreator;
import com.patex.utils.Res;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.patex.extlib.ExtLibService.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class
)
public class ExtLibServiceTest {

    private static final Long SUBSCRIBE_ID = 654L;
    private static final String LIBRARY_NAME = "libraryName";
    private static final String PREFIX = "prefix";
    private static final String URI = "uri";
    private static final String TYPE = "type";
    private static final long ID = 42L;
    private static final long BOOK_ID = 4242L;

    @Mock
    private ExtLibraryRepository repository;

    @Mock
    private ZUserService userService;

    @Mock
    private ExtLibDownloadService downloadService;

    @Mock
    private ExtLibSubscriptionService subscriptionService;

    @SuppressWarnings("unused")
    @Mock
    private ExecutorCreator executorCreator;

    @InjectMocks
    private ExtLibService extLibService;

    private ExtLibrary library;
    private ZUser user = new ZUser();
    private String entryUrl = "entryUrl";

    @Before
    public void setUp() {
        library = new ExtLibrary();
        library.setId(ID);
        library.setName(LIBRARY_NAME);
        Book book = new Book();
        book.setId(BOOK_ID);

        when(repository.findAll()).thenReturn(Collections.singleton(library));
        when(repository.findById(ID)).thenReturn(Optional.of(library));

        when(userService.getCurrentUser()).thenReturn(user);
        when(downloadService.downloadBook(library, URI, TYPE, user)).thenReturn(book);


        final Object[] objects = new Object[]{};
        final Res entryTitle = new Res("entryTitle", objects);
        OPDSEntry entry = new OPDSEntryBuilder("entryId", new Date(), entryTitle)
                .addLink("linHref", OPDSLink.FB2)
                .build();
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.singletonList(entry), Collections.emptyList());
        when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

    }

    @Test
    public void shouldReturnRootEntries() {


        List<OPDSEntry> roots = extLibService.getRoot(PREFIX);

        assertThat(roots, hasSize(1));
        OPDSEntry root = roots.get(0);
        assertEquals(LIBRARY_NAME, root.getTitle().getObjs()[0]);

        assertTrue("Should contain prefix", root.getLinks().stream().map(OPDSLink::getHref).
                anyMatch(href -> href.contains(PREFIX)));
        assertTrue("Should contain id", root.getLinks().stream().map(OPDSLink::getHref).
                anyMatch(href -> href.contains("" + ID)));
    }

    @Test
    public void shouldDownloadBook() {

        String bookId = extLibService.downloadBook(ID, URI, TYPE);

        assertTrue(bookId.contains("" + BOOK_ID));
    }

    @Test
    public void shouldDownloadAll() {
        String downloadAllURL = "downloadAllURL";

        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, downloadAllURL);
        extLibService.actionExtLibData(ID, Action.downloadAll.name(), params);

        verify(downloadService).downloadAll(library, downloadAllURL, user);
    }

    @Test
    public void shouldSubscribe() {
        String subscribeURL = "subscribeURL";

        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, subscribeURL);
        extLibService.actionExtLibData(ID, Action.subscribe.name(), params);

        verify(subscriptionService).addSubscription(library, subscribeURL);
    }

    @Test
    public void shouldUnsubscribe() {

        HashMap<String, String> params = new HashMap<>();
        params.put("id", "" + SUBSCRIBE_ID);
        extLibService.actionExtLibData(ID, Action.unsubscribe.name(), params);

        verify(subscriptionService).deleteSubscription(SUBSCRIBE_ID);
    }

    @Test
    public void shouldHaveNoAdditionalEntries() {
        String entryUrl = "entryUrl";
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);

        final Object[] objects = new Object[]{};
        final Res entryTitle = new Res("entryTitle", objects);
        OPDSEntry entry = new OPDSEntryBuilder("dd", new Date(), entryTitle).build();
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.singletonList(entry), Collections.emptyList());
        when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        assertThat(feed.getEntries(), hasSize(1));
    }

    @Test
    public void shouldHaveDownloadAllEntry() {

        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(DOWNLOAD_ID_PREFIX)).findFirst();

        assertTrue(entryO.isPresent());
    }

    @Test
    public void shouldHaveSubscribeEntry() {
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(SUBSCRIBE_ID_PREFIX)).findFirst();

        assertTrue(entryO.isPresent());
    }


    @Test
    public void shouldHaveUnSubscribeEntry() {
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);
        Subscription subscription = new Subscription();
        subscription.setId(92L);
        when(subscriptionService.find(library, entryUrl)).thenReturn(Optional.of(subscription));

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);


        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(UNSUBSCRIBE_ID_PREFIX)).findFirst();
        assertTrue(entryO.isPresent());
    }


    @Test
    public void shouldHaveNextEntry() {
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.emptyList(), Collections.singletonList(new OPDSLink("dd", REL_NEXT, "type")));
        when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(NEXT_ID_PREFIX)).findFirst();
        assertTrue(entryO.isPresent());
    }
}
