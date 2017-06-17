package com.patex.extlib;

import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.messaging.MessengerService;
import com.patex.opds.OPDSEntryI;
import com.patex.opds.OPDSLink;
import com.patex.service.BookService;
import com.patex.service.ZUserService;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by Alexey on 11.03.2017.
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
public class ExtLibTest {

    private String url;
    private String uri;
    private String opdsPath;
    private ExtLib extLib;
    private BookService bookService;
    private ExtLibConnection connectionService;
    private ExtLibConnection.ExtlibCon extlibConnection;
    private SyndFeedImpl syndFeed;
    private SyndLinkImpl syndLink;
    private SyndContentImpl syndContent;
    private SyndEntryImpl syndEntry;
    private ExtLibrary extLibrary;

    @Before
    public void setUp() throws Exception {

        url = "http://" + RandomStringUtils.randomAlphabetic(10);
        uri = RandomStringUtils.randomAlphabetic(10);
        opdsPath = RandomStringUtils.randomAlphabetic(10);

        extLibrary = new ExtLibrary();
        extLibrary.setUrl(url);
        extLibrary.setOpdsPath(opdsPath);
        bookService = mock(BookService.class);
        connectionService = mock(ExtLibConnection.class);
        extlibConnection = mock(ExtLibConnection.ExtlibCon.class);
        when(connectionService.openConnection(url + uri)).thenReturn(extlibConnection);
        extLib = createExtLib(extLibrary);

        syndFeed = new SyndFeedImpl();
        syndFeed.setTitle(RandomStringUtils.randomAlphabetic(10));
        syndEntry = new SyndEntryImpl();
        syndEntry.setUri(RandomStringUtils.randomAlphabetic(10));
        syndEntry.setTitle(RandomStringUtils.randomAlphabetic(10));

        syndLink = new SyndLinkImpl();
        syndLink.setType("profile=opds-catalog");
        syndLink.setHref(RandomStringUtils.randomAlphabetic(10));
        syndLink.setRel(RandomStringUtils.randomAlphabetic(10));
        syndEntry.setLinks(Collections.singletonList(syndLink));

        syndContent = new SyndContentImpl();
        syndContent.setType(RandomStringUtils.randomAlphabetic(10));
        syndContent.setValue(RandomStringUtils.randomAlphabetic(10));
        syndContent.setMode("xml");
        syndEntry.setContents(Collections.singletonList(syndContent));

        syndFeed.setEntries(Collections.singletonList(syndEntry));
        when(extlibConnection.getData(any())).thenReturn(syndFeed);
    }

    @Test
    public void testGetDataSimpleFeed() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put(ExtLib.REQUEST_P_NAME, this.uri);
        ExtLibFeed data = extLib.getExtLibFeed(map);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        assertThat("ExtLibFeed entries size", data.getEntries(), hasSize(1));
        OPDSEntryI entry = data.getEntries().get(0);
        checkSyndEntry(syndEntry, entry);

        assertThat("Entry other link size", entry.getLinks(), hasSize(1));
        OPDSLink link = entry.getLinks().get(0);
        checkLync(syndLink, link);

        assertThat("entry contents size", entry.getContent().get(), hasSize(1));
        String content = entry.getContent().get().get(0);
        checkContent(syndContent, content);
    }

    @Test
    public void testGetDataFeedWithFB2() throws Exception {
        SyndLinkImpl syndLink = new SyndLinkImpl();
        syndLink.setType("application/fb2");
        syndLink.setHref(RandomStringUtils.randomAlphabetic(10));
        syndLink.setRel(RandomStringUtils.randomAlphabetic(10));
        syndEntry.setLinks(Collections.singletonList(syndLink));

        HashMap<String, String> map = new HashMap<>();
        map.put(ExtLib.REQUEST_P_NAME, this.uri);
        ExtLibFeed data = extLib.getExtLibFeed(map);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        assertThat("ExtLibFeed entries size", data.getEntries(), hasSize(3));
        OPDSEntryI entry = data.getEntries().get(2);
        checkSyndEntry(syndEntry, entry);

        assertThat("Entry other link size", entry.getLinks(), hasSize(1));
        OPDSLink link = entry.getLinks().get(0);
        assertEquals("Link Href", ExtLibOPDSEntry.mapToUri("action/download?type=fb2&", syndLink.getHref()), link.getHref());
        assertEquals("Link Rel", syndLink.getRel(), link.getRel());

        assertThat("entry contents size", entry.getContent().get(), hasSize(1));
        String content = entry.getContent().get().get(0);
        checkContent(syndContent, content);
    }

    @Test
    public void testGetDataFeedWithNextLink() throws Exception {

        SyndLinkImpl syndLinkNext = new SyndLinkImpl();
        syndLinkNext.setRel(ExtLib.REL_NEXT);
        syndLinkNext.setHref(RandomStringUtils.randomAlphabetic(10));
        syndLinkNext.setType("profile=opds-catalog");
        syndFeed.setLinks(Collections.singletonList(syndLinkNext));

        HashMap<String, String> map = new HashMap<>();
        map.put(ExtLib.REQUEST_P_NAME, this.uri);
        ExtLibFeed data = extLib.getExtLibFeed(map);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        assertThat("ExtLibFeed entries size", data.getEntries(), hasSize(2));
        OPDSEntryI entry = data.getEntries().get(0);
        checkSyndEntry(syndEntry, entry);

        assertThat("Entry other link size", entry.getLinks(), hasSize(1));
        OPDSLink link = entry.getLinks().get(0);
        checkLync(syndLink, link);

        assertThat("entry contents size", entry.getContent().get(), hasSize(1));
        String content = entry.getContent().get().get(0);
        checkContent(syndContent, content);

        OPDSEntryI nextEntry = data.getEntries().get(1);
        assertEquals("Entry next Title", "Next", nextEntry.getTitle());

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
        String uri1 = RandomStringUtils.randomAlphabetic(10);
        String uri2 = RandomStringUtils.randomAlphabetic(10);


        SyndEntryImpl syndEntry1 = new SyndEntryImpl();
        syndEntry1.setUri(RandomStringUtils.randomAlphabetic(10));
        syndEntry1.setTitle("First:" + RandomStringUtils.randomAlphabetic(10));

        SyndLinkImpl syndLink1 = new SyndLinkImpl();
        syndLink1.setType(ExtLib.FB2_TYPE);
        syndLink1.setHref(uri1);
        syndLink1.setRel(RandomStringUtils.randomAlphabetic(10));
        syndEntry1.setLinks(Collections.singletonList(syndLink1));

        SyndContentImpl syndContent1 = new SyndContentImpl();
        syndContent1.setType(RandomStringUtils.randomAlphabetic(10));
        syndContent1.setValue(RandomStringUtils.randomAlphabetic(10));
        syndContent1.setMode("xml");
        syndEntry1.setContents(Collections.singletonList(syndContent1));


        SyndEntryImpl syndEntry2 = new SyndEntryImpl();
        syndEntry2.setUri(RandomStringUtils.randomAlphabetic(10));
        syndEntry2.setTitle("Second:" + RandomStringUtils.randomAlphabetic(10));

        SyndLinkImpl syndLink2 = new SyndLinkImpl();
        syndLink2.setType(ExtLib.FB2_TYPE);
        syndLink2.setHref(uri2);
        syndLink2.setRel(RandomStringUtils.randomAlphabetic(10));
        syndEntry2.setLinks(Collections.singletonList(syndLink2));

        SyndContentImpl syndContent2 = new SyndContentImpl();
        syndContent2.setType(RandomStringUtils.randomAlphabetic(10));
        syndContent2.setValue(RandomStringUtils.randomAlphabetic(10));
        syndContent2.setMode("xml");
        syndEntry2.setContents(Collections.singletonList(syndContent2));

        connectionService = spy(ExtLibConnection.class);

        ExtLibConnection.ExtlibCon extlibConnection1 =
                spy(connectionService.new ExtlibCon("http://" + RandomStringUtils.randomAlphabetic(10)));
        URLConnection urlConnection1 = mock(URLConnection.class);
        String fileName1 = RandomStringUtils.randomAlphabetic(10);
        when(urlConnection1.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName1 + "\"");
        InputStream isMock1 = mock(InputStream.class);
        when(urlConnection1.getInputStream()).thenReturn(isMock1);
        Book book1 = new Book();
        book1.setId(RandomUtils.nextLong(0, 1000));
        when(bookService.uploadBook(fileName1, isMock1)).thenReturn(book1);
        when(extlibConnection1.getConnection()).thenReturn(urlConnection1);
        when(connectionService.openConnection(url + uri1)).thenReturn(extlibConnection1);

        ExtLibConnection.ExtlibCon extlibConnection2 =
                spy(connectionService.new ExtlibCon("http://" + RandomStringUtils.randomAlphabetic(10)));
        URLConnection urlConnection2 = mock(URLConnection.class);
        String fileName2 = RandomStringUtils.randomAlphabetic(10);
        when(urlConnection2.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName2 + "\"");
        InputStream isMock2 = mock(InputStream.class);
        when(urlConnection2.getInputStream()).thenReturn(isMock2);
        Book book2 = new Book();
        book2.setId(RandomUtils.nextLong(0, 1000));
        when(bookService.uploadBook(fileName2, isMock2)).thenReturn(book2);
        when(extlibConnection2.getConnection()).thenReturn(urlConnection2);
        when(connectionService.openConnection(url + uri2)).thenReturn(extlibConnection2);

        syndFeed.setEntries(Arrays.asList(syndEntry1, syndEntry2));

        when(connectionService.openConnection(url + uri)).thenReturn(extlibConnection);
        when(extlibConnection.getData(any())).thenReturn(syndFeed);

        extLib = createExtLib(extLibrary);
        HashMap<String, String> params = new HashMap<>();
        String type = RandomStringUtils.randomAlphabetic(10);
        params.put(ExtLib.PARAM_TYPE, type);
        params.put(ExtLib.REQUEST_P_NAME, this.uri);
        extLib.action(ExtLib.ACTION_DOWNLOAD_ALL, params);
        Thread.sleep(300);
        verify(bookService, times(1)).uploadBook(fileName1, isMock1);
        verify(bookService, times(1)).uploadBook(fileName2, isMock2);
        verifyNoMoreInteractions(bookService);
    }

    private ExtLib createExtLib(ExtLibrary extLibrary) {
        ExtLib extLib = new ExtLib(extLibrary);
        ReflectionTestUtils.setField(extLib, "extLibConnectionService", connectionService);
        ReflectionTestUtils.setField(extLib, "bookService", bookService);
        ReflectionTestUtils.setField(extLib, "messengerService", mock(MessengerService.class));
        ReflectionTestUtils.setField(extLib, "userService", mock(ZUserService.class));

        return extLib;
    }

    @Test
    public void testDownloadAction() throws Exception {
        String uri = RandomStringUtils.randomAlphabetic(10);
        connectionService = spy(ExtLibConnection.class);
        extlibConnection = spy(connectionService.new ExtlibCon("http://" + RandomStringUtils.randomAlphabetic(10)));
        extLib = createExtLib(extLibrary);
        URLConnection urlConnection = mock(URLConnection.class);
        String fileName = RandomStringUtils.randomAlphabetic(10);
        when(urlConnection.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName + "\"");
        InputStream isMock = mock(InputStream.class);
        when(urlConnection.getInputStream()).thenReturn(isMock);
        Book book = new Book();
        book.setId(RandomUtils.nextLong(0, 1000));
        when(bookService.uploadBook(fileName, isMock)).thenReturn(book);
        when(extlibConnection.getConnection()).thenReturn(urlConnection);
        when(connectionService.openConnection(url + uri)).thenReturn(extlibConnection);
        HashMap<String, String> params = new HashMap<>();
        String type = RandomStringUtils.randomAlphabetic(10);
        params.put(ExtLib.PARAM_TYPE, type);
        params.put(ExtLib.REQUEST_P_NAME, uri);
        extLib.action(ExtLib.ACTION_DOWNLOAD, params);
        verify(bookService, only()).uploadBook(fileName, isMock);
    }

    private void checkLync(SyndLinkImpl syndLink, OPDSLink link) {
        assertEquals("Link Href", ExtLibOPDSEntry.mapToUri("?", syndLink.getHref()), link.getHref());
        assertEquals("Link Rel", syndLink.getRel(), link.getRel());
    }

    private void checkContent(SyndContentImpl syndContent, String content) {
//        assertEquals("Content Type", syndContent.getType(), content.getType());
        assertEquals("Content Value", syndContent.getValue(), content);
//        assertEquals("Content Mode", syndContent.getMode(), content.getMode());
    }

    private void checkSyndEntry(SyndEntryImpl syndEntry, OPDSEntryI entry) {
        assertEquals("Entry Title", syndEntry.getTitle(), entry.getTitle());
        assertEquals("Entry Id", syndEntry.getUri(), entry.getId());
    }
}
