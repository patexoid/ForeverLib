package com.patex.forever.opds;

import com.patex.forever.model.Book;
import com.patex.forever.model.Res;
import com.patex.forever.model.User;
import com.patex.forever.opds.entity.ExtLibrary;
import com.patex.forever.opds.entity.ExtLibraryRepository;
import com.patex.forever.opds.entity.SubscriptionEntity;
import com.patex.forever.opds.model.ExtLibFeed;
import com.patex.forever.opds.model.OPDSEntry;
import com.patex.forever.opds.model.OPDSEntryBuilder;
import com.patex.forever.opds.model.OPDSLink;
import com.patex.forever.opds.service.ExtLibDownloadService;
import com.patex.forever.opds.service.ExtLibService;
import com.patex.forever.opds.service.ExtLibSubscriptionService;
import com.patex.forever.service.ExecutorCreator;
import com.patex.forever.service.UserService;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.patex.forever.opds.service.ExtLibService.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
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
    private UserService userService;

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
    private User user = new User();
    private String entryUrl = "entryUrl";

    @BeforeEach
    public void setUp() {
        library = new ExtLibrary();
        library.setId(ID);
        library.setName(LIBRARY_NAME);
        Book book = new Book();
        book.setId(BOOK_ID);

        lenient().when(repository.findAll()).thenReturn(Collections.singleton(library));
        lenient().when(repository.findById(ID)).thenReturn(Optional.of(library));

        lenient().when(userService.getCurrentUser()).thenReturn(user);
        lenient().when(downloadService.downloadBook(library, URI, TYPE, user.getUsername())).thenReturn(book);


        final Object[] objects = new Object[]{};
        final Res entryTitle = new Res("entryTitle", objects);
        OPDSEntry entry = new OPDSEntryBuilder("entryId", Instant.now(), entryTitle)
                .addLink("linHref", OPDSLink.FB2)
                .build();
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.singletonList(entry), Collections.emptyList());
        lenient().when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

    }

    @Test
    public void shouldReturnRootEntries() {


        List<OPDSEntry> roots = extLibService.getRoot(PREFIX);

        assertThat(roots, IsCollectionWithSize.hasSize(1));
        OPDSEntry root = roots.get(0);
        assertEquals(LIBRARY_NAME, root.getTitle().getObjs()[0]);

        assertTrue(root.getLinks().stream().map(OPDSLink::getHref).
                        anyMatch(href -> href.contains(PREFIX)), "Should contain prefix");
        assertTrue(root.getLinks().stream().map(OPDSLink::getHref).
                        anyMatch(href -> href.contains("" + ID)), "Should contain id");
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
        extLibService.actionExtLibData(ID, ExtLibService.Action.downloadAll.name(), params);

        Mockito.verify(downloadService).downloadAll(library, downloadAllURL, user.getUsername());
    }

    @Test
    public void shouldSubscribe() {
        String subscribeURL = "subscribeURL";

        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, subscribeURL);
        extLibService.actionExtLibData(ID, ExtLibService.Action.subscribe.name(), params);

        Mockito.verify(subscriptionService).addSubscription(library, subscribeURL);
    }

    @Test
    public void shouldUnsubscribe() {

        HashMap<String, String> params = new HashMap<>();
        params.put("id", "" + SUBSCRIBE_ID);
        extLibService.actionExtLibData(ID, ExtLibService.Action.unsubscribe.name(), params);

        Mockito.verify(subscriptionService).deleteSubscription(SUBSCRIBE_ID);
    }

    @Test
    public void shouldHaveNoAdditionalEntries() {
        String entryUrl = "entryUrl";
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);

        final Object[] objects = new Object[]{};
        final Res entryTitle = new Res("entryTitle", objects);
        OPDSEntry entry = new OPDSEntryBuilder("dd", Instant.now(), entryTitle).build();
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.singletonList(entry), Collections.emptyList());
        when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        assertThat(feed.getEntries(), IsCollectionWithSize.hasSize(1));
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
        SubscriptionEntity subscription = new SubscriptionEntity();
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
