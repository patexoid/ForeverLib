package com.patex.zombie.opds;

import com.patex.zombie.model.Book;
import com.patex.zombie.model.Res;
import com.patex.zombie.model.User;
import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.ExtLibraryRepository;
import com.patex.zombie.opds.entity.SubscriptionEntity;
import com.patex.zombie.opds.model.ExtLibFeed;
import com.patex.zombie.opds.model.OPDSEntryBuilder;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import com.patex.zombie.opds.service.ExtLibDownloadService;
import com.patex.zombie.opds.service.ExtLibService;
import com.patex.zombie.opds.service.ExtLibSubscriptionService;
import com.patex.zombie.service.ExecutorCreator;
import com.patex.zombie.service.UserService;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.patex.zombie.opds.service.ExtLibService.*;
import static org.junit.Assert.assertEquals;


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

    @Before
    public void setUp() {
        library = new ExtLibrary();
        library.setId(ID);
        library.setName(LIBRARY_NAME);
        Book book = new Book();
        book.setId(BOOK_ID);

        Mockito.when(repository.findAll()).thenReturn(Collections.singleton(library));
        Mockito.when(repository.findById(ID)).thenReturn(Optional.of(library));

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(downloadService.downloadBook(library, URI, TYPE, user.getUsername())).thenReturn(book);


        final Object[] objects = new Object[]{};
        final Res entryTitle = new Res("entryTitle", objects);
        OPDSEntry entry = new OPDSEntryBuilder("entryId", Instant.now(), entryTitle)
                .addLink("linHref", OPDSLink.FB2)
                .build();
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.singletonList(entry), Collections.emptyList());
        Mockito.when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

    }

    @Test
    public void shouldReturnRootEntries() {


        List<OPDSEntry> roots = extLibService.getRoot(PREFIX);

        Assert.assertThat(roots, IsCollectionWithSize.hasSize(1));
        OPDSEntry root = roots.get(0);
        assertEquals(LIBRARY_NAME, root.getTitle().getObjs()[0]);

        Assert.assertTrue("Should contain prefix", root.getLinks().stream().map(OPDSLink::getHref).
                anyMatch(href -> href.contains(PREFIX)));
        Assert.assertTrue("Should contain id", root.getLinks().stream().map(OPDSLink::getHref).
                anyMatch(href -> href.contains("" + ID)));
    }

    @Test
    public void shouldDownloadBook() {

        String bookId = extLibService.downloadBook(ID, URI, TYPE);

        Assert.assertTrue(bookId.contains("" + BOOK_ID));
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
        Mockito.when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Assert.assertThat(feed.getEntries(), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void shouldHaveDownloadAllEntry() {

        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(DOWNLOAD_ID_PREFIX)).findFirst();

        Assert.assertTrue(entryO.isPresent());
    }

    @Test
    public void shouldHaveSubscribeEntry() {
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(SUBSCRIBE_ID_PREFIX)).findFirst();

        Assert.assertTrue(entryO.isPresent());
    }


    @Test
    public void shouldHaveUnSubscribeEntry() {
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(92L);
        Mockito.when(subscriptionService.find(library, entryUrl)).thenReturn(Optional.of(subscription));

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);


        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(UNSUBSCRIBE_ID_PREFIX)).findFirst();
        Assert.assertTrue(entryO.isPresent());
    }


    @Test
    public void shouldHaveNextEntry() {
        HashMap<String, String> params = new HashMap<>();
        params.put(REQUEST_P_NAME, entryUrl);
        ExtLibFeed rawFeed = new ExtLibFeed("title",
                Collections.emptyList(), Collections.singletonList(new OPDSLink("dd", REL_NEXT, "type")));
        Mockito.when(downloadService.getExtLibFeed(library, entryUrl)).thenReturn(rawFeed);

        ExtLibFeed feed = extLibService.getDataForLibrary(ID, params);

        Optional<OPDSEntry> entryO = feed.getEntries().stream()
                .filter(e -> e.getId().startsWith(NEXT_ID_PREFIX)).findFirst();
        Assert.assertTrue(entryO.isPresent());
    }
}
