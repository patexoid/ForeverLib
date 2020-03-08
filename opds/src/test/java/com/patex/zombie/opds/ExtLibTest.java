package com.patex.zombie.opds;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.entities.BookEntity;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.service.BookService;
import com.patex.service.TransactionService;
import com.patex.utils.ExecutorCreator;
import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.SavedBookRepository;
import com.patex.zombie.opds.model.ExtLibFeed;
import com.patex.zombie.opds.model.OPDSContent;
import com.patex.zombie.opds.model.converter.LinkMapper;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import com.patex.zombie.opds.service.ExtLibConnection;
import com.patex.zombie.opds.service.ExtLibDownloadService;
import com.patex.zombie.opds.service.ExtLibInScopeRunner;
import com.patex.zombie.opds.service.ExtLibScopeStorage;
import com.patex.zombie.opds.service.ExtLibService;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import com.rometools.rome.io.SyndFeedOutput;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alexey on 11.03.2017.
 */
@Ignore
public class ExtLibTest {

    private final RandomStringGenerator rsg = new RandomStringGenerator.Builder()
            .withinRange('a', 'z').build();
    private String url;
    private String uri;
    private String opdsPath;
    private ExtLibDownloadService downloadService;
    private BookService bookService;
    private ExtLibConnection connectionService;
    private SyndFeedImpl syndFeed;
    private SyndLinkImpl syndLink;
    private SyndContentImpl syndContent;
    private SyndEntryImpl syndEntry;
    private ExtLibrary extLibrary;
    private ExecutorCreator executorCreator;

    @Before
    public void setUp() {

        url = "http://" + rsg.generate(10);
        uri = rsg.generate(10);
        opdsPath = rsg.generate(10);

        extLibrary = new ExtLibrary();
        extLibrary.setUrl(url);
        extLibrary.setOpdsPath(opdsPath);
        bookService = Mockito.mock(BookService.class);
        connectionService = Mockito.mock(ExtLibConnection.class);
        downloadService = createExtLib();

        syndFeed = new SyndFeedImpl();
        syndFeed.setTitle(rsg.generate(10));
        syndEntry = new SyndEntryImpl();
        syndEntry.setUri(rsg.generate(10));
        syndEntry.setTitle(rsg.generate(10));

        syndLink = new SyndLinkImpl();
        syndLink.setType("profile=opds-catalog");
        syndLink.setHref(rsg.generate(10));
        syndLink.setRel(rsg.generate(10));
        syndEntry.setLinks(Collections.singletonList(syndLink));

        syndContent = new SyndContentImpl();
        syndContent.setType(rsg.generate(10));
        syndContent.setValue(rsg.generate(10));
        syndContent.setMode("xml");
        syndEntry.setContents(Collections.singletonList(syndContent));

        syndFeed.setEntries(Collections.singletonList(syndEntry));
//        when(connectionService.getFeed(uri)).thenReturn(syndFeed);

        executorCreator = Mockito.mock(ExecutorCreator.class);
        Mockito.when(executorCreator.createExecutor(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(MoreExecutors.newDirectExecutorService());
    }

    @Test
    @Ignore
    public void testGetDataSimpleFeed() {
        ExtLibFeed data = downloadService.getExtLibFeed(extLibrary, uri);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        Assert.assertThat("ExtLibFeed entries size", data.getEntries(), IsCollectionWithSize.hasSize(1));
        OPDSEntry entry = data.getEntries().get(0);
        checkSyndEntry(syndEntry, entry);

        Assert.assertThat("Entry other link size", entry.getLinks(), IsCollectionWithSize.hasSize(1));
        OPDSLink link = entry.getLinks().get(0);
        checkLync(syndLink, link);

        Assert.assertThat("entry contents size", entry.getContent(), IsCollectionWithSize.hasSize(1));
        OPDSContent content = entry.getContent().get(0);
        checkContent(syndContent, content);
    }

    @Test
    public void testGetDataFeedWithFB2() {
        SyndLinkImpl syndLink = new SyndLinkImpl();
        syndLink.setType("application/fb2");
        syndLink.setHref(rsg.generate(10));
        syndLink.setRel(rsg.generate(10));
        syndEntry.setLinks(Collections.singletonList(syndLink));

        ExtLibFeed data = downloadService.getExtLibFeed(extLibrary, uri);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        Assert.assertThat("ExtLibFeed entries size", data.getEntries(), IsCollectionWithSize.hasSize(3));
        OPDSEntry entry = data.getEntries().get(2);
        checkSyndEntry(syndEntry, entry);

        Assert.assertThat("Entry other link size", entry.getLinks(), IsCollectionWithSize.hasSize(1));
        OPDSLink link = entry.getLinks().get(0);
        assertEquals("Link Href", LinkMapper.mapToUri("action/download?type=fb2&", syndLink.getHref()), link.getHref());
        assertEquals("Link Rel", syndLink.getRel(), link.getRel());

        Assert.assertThat("entry contents size", entry.getContent(), IsCollectionWithSize.hasSize(1));
        checkContent(syndContent, entry.getContent().get(0));
    }

    @Test
    public void testGetDataFeedWithNextLink() {

        SyndLinkImpl syndLinkNext = new SyndLinkImpl();
        syndLinkNext.setRel(ExtLibService.REL_NEXT);
        syndLinkNext.setHref(rsg.generate(10));
        syndLinkNext.setType("profile=opds-catalog");
        syndFeed.setLinks(Collections.singletonList(syndLinkNext));

        ExtLibFeed data = downloadService.getExtLibFeed(extLibrary, uri);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        Assert.assertThat("ExtLibFeed entries size", data.getEntries(), IsCollectionWithSize.hasSize(2));
        OPDSEntry entry = data.getEntries().get(0);
        checkSyndEntry(syndEntry, entry);

        Assert.assertThat("Entry other link size", entry.getLinks(), IsCollectionWithSize.hasSize(1));
        OPDSLink link = entry.getLinks().get(0);
        checkLync(syndLink, link);

        Assert.assertThat("entry contents size", entry.getContent(), IsCollectionWithSize.hasSize(1));
        checkContent(syndContent, entry.getContent().get(0));

        OPDSEntry nextEntry = data.getEntries().get(1);
        assertEquals("opds.extlib.nextPage", nextEntry.getTitle().getKey());

//        Content nextContent = nextEntry.getContent().get(0);
//        assertEquals("Content Type", "html", nextContent.getType());
//        assertEquals("Content Value", "Next Page", nextContent.getValue());

        OPDSLink nextEntryLink = nextEntry.getLinks().get(0);
        OPDSLink nextFeedLink = data.getLinks().get(0);
        checkLync(syndLinkNext, nextEntryLink);
        checkLync(syndLinkNext, nextFeedLink);
    }

    @Test
    public void testDownloadAllAction() throws Exception {
        String uri1 = "book1_" + rsg.generate(10);
        String uri2 = "book2_" + rsg.generate(10);


        SyndEntryImpl syndEntry1 = new SyndEntryImpl();
        syndEntry1.setUri(rsg.generate(10));
        syndEntry1.setTitle("First:" + rsg.generate(10));

        SyndLinkImpl syndLink1 = new SyndLinkImpl();
        syndLink1.setType(ExtLibService.FB2_TYPE);
        syndLink1.setHref(uri1);
        syndLink1.setRel(rsg.generate(10));
        syndEntry1.setLinks(Collections.singletonList(syndLink1));

        SyndContentImpl syndContent1 = new SyndContentImpl();
        syndContent1.setType(rsg.generate(10));
        syndContent1.setValue(rsg.generate(10));
        syndContent1.setMode("xml");
        syndEntry1.setContents(Collections.singletonList(syndContent1));


        SyndEntryImpl syndEntry2 = new SyndEntryImpl();
        syndEntry2.setUri(rsg.generate(10));
        syndEntry2.setTitle("Second:" + rsg.generate(10));

        SyndLinkImpl syndLink2 = new SyndLinkImpl();
        syndLink2.setType(ExtLibService.FB2_TYPE);
        syndLink2.setHref(uri2);
        syndLink2.setRel(rsg.generate(10));
        syndEntry2.setLinks(Collections.singletonList(syndLink2));

        SyndContentImpl syndContent2 = new SyndContentImpl();
        syndContent2.setType(rsg.generate(10));
        syndContent2.setValue(rsg.generate(10));
        syndContent2.setMode("xml");
        syndEntry2.setContents(Collections.singletonList(syndContent2));

        connectionService =
                Mockito.spy(new ExtLibConnection(url, "", null, null, null, 0, null,
                        executorCreator, bookService, 300));
        downloadService = createExtLib();
        URLConnection urlConnection1 = Mockito.mock(URLConnection.class);
        String fileName1 = rsg.generate(10);
        Mockito.when(urlConnection1.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName1 + "\"");
        InputStream isMock1 = Mockito.mock(InputStream.class);
        Mockito.when(urlConnection1.getInputStream()).thenReturn(isMock1);
        BookEntity book1 = new BookEntity();
        book1.setId(RandomUtils.nextLong(0, 1000));
        Mockito.when(connectionService.getConnection(url + uri1)).thenReturn(urlConnection1);


        URLConnection urlConnection2 = Mockito.mock(URLConnection.class);
        String fileName2 = rsg.generate(10);
        Mockito.when(urlConnection2.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName2 + "\"");
        InputStream isMock2 = Mockito.mock(InputStream.class);
        Mockito.when(urlConnection2.getInputStream()).thenReturn(isMock2);
        BookEntity book2 = new BookEntity();
        book2.setId(RandomUtils.nextLong(0, 1000));
        Mockito.when(connectionService.getConnection(url + uri2)).thenReturn(urlConnection2);

        syndFeed.setEntries(Arrays.asList(syndEntry1, syndEntry2));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        syndFeed.setFeedType("atom_1.0");
        new SyndFeedOutput().output(syndFeed, new OutputStreamWriter(baos));
        URLConnection urlConnectionFeed = Mockito.mock(URLConnection.class);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Mockito.when(urlConnectionFeed.getInputStream()).thenReturn(bais);
        Mockito.when(connectionService.getConnection(url + uri)).thenReturn(urlConnectionFeed);


        bais.reset();
        ZUser user = new ZUser();
        downloadService.downloadAll(extLibrary, uri, user);
        Thread.sleep(3000);
    }

    private ExtLibDownloadService createExtLib() {
        return new ExtLibDownloadService(
                connectionService,
                new ExtLibInScopeRunner(Mockito.mock(ExtLibScopeStorage.class)),
                Mockito.mock(SavedBookRepository.class), Mockito.mock(MessengerService.class), executorCreator,
                new TransactionService());
    }

    @Test
    public void testDownloadAction() throws Exception {
        String uri = rsg.generate(10);
        connectionService = Mockito.spy(new ExtLibConnection(url, "", null, null, null, 0, null,
                executorCreator, bookService, 300));
        URLConnection urlConnection = Mockito.mock(URLConnection.class);
        String fileName = rsg.generate(10);
        Mockito.when(urlConnection.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName + "\"");
        InputStream isMock = Mockito.mock(InputStream.class);
        Mockito.when(urlConnection.getInputStream()).thenReturn(isMock);
        BookEntity book = new BookEntity();
        book.setId(RandomUtils.nextLong(0, 1000));
        ZUser user = new ZUser();
        Mockito.when(connectionService.getConnection(url + uri)).thenReturn(urlConnection);
        String type = rsg.generate(10);
        downloadService.downloadBook(extLibrary, uri, type, user);
    }

    private void checkLync(SyndLinkImpl syndLink, OPDSLink link) {
        assertEquals("Link Href", LinkMapper.mapToUri("?", syndLink.getHref()), link.getHref());
        assertEquals("Link Rel", syndLink.getRel(), link.getRel());
    }

    private void checkContent(SyndContentImpl syndContent, OPDSContent content) {
        assertEquals("Content Type", syndContent.getType(), content.getType());
        assertEquals("Content Value", syndContent.getValue(), content.getValue());
//        assertEquals("Content Mode", syndContent.getMode(), content.getSrc());
    }

    private void checkSyndEntry(SyndEntryImpl syndEntry, OPDSEntry entry) {
        assertEquals("Entry Title", syndEntry.getTitle(), entry.getTitle().getObjs()[0]);
        assertEquals("Entry Id", syndEntry.getUri(), entry.getId());
    }
}
