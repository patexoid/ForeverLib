package com.patex.extlib;

import com.patex.entities.ExtLibrary;
import com.patex.service.BookService;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Alexey on 11.03.2017.
 */
public class ExtLibTest {

    private String url;
    private String uri;
    private String opdsPath;
    private ExtLib extLib;
    private BookService bookService;
    private ExtLibConnectionService connectionService;
    private ExtLibConnectionService.ExtlibCon extlibConnection;
    private SyndFeedImpl syndFeed;
    private SyndLinkImpl syndLink;
    private SyndContentImpl syndContent;
    private SyndEntryImpl syndEntry;

    @Before
    public void setUp() throws Exception {

        url = RandomStringUtils.randomAlphabetic(10);
        uri = RandomStringUtils.randomAlphabetic(10);
        opdsPath = RandomStringUtils.randomAlphabetic(10);

        ExtLibrary extLibrary = new ExtLibrary();
        extLibrary.setUrl(url);
        extLibrary.setOpdsPath(opdsPath);
        bookService = mock(BookService.class);
        connectionService = mock(ExtLibConnectionService.class);
        extlibConnection = mock(ExtLibConnectionService.ExtlibCon.class);
        when(connectionService.openConnection(url + uri)).thenReturn(extlibConnection);
        extLib = new ExtLib(extLibrary, bookService, connectionService);

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
        ExtLibFeed data = extLib.getData(map);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        assertThat("ExtLibFeed entries size", data.getEntries(), hasSize(1));
        Entry entry = data.getEntries().get(0);
        checkSyndEntry(syndEntry, entry);

        assertThat("Entry other link size", entry.getOtherLinks(), hasSize(1));
        Link link = entry.getOtherLinks().get(0);
        checkLync(syndLink, link);

        assertThat("entry contents size", entry.getContents(), hasSize(1));
        Content content = entry.getContents().get(0);
        checkContent(syndContent, content);
    }

    @Test
    public void testGetDataFeedWithFB2() throws Exception {
        SyndLinkImpl syndLink = new SyndLinkImpl();
        syndLink.setType("application/fb2");
        syndLink.setHref(RandomStringUtils.randomAlphabetic(10));
        syndLink.setRel(RandomStringUtils.randomAlphabetic(10));
        syndEntry.setLinks(Collections.singletonList(syndLink));

        syndFeed.setEntries(Collections.singletonList(syndEntry));
        when(extlibConnection.getData(any())).thenReturn(syndFeed);

        HashMap<String, String> map = new HashMap<>();
        map.put(ExtLib.REQUEST_P_NAME, this.uri);
        ExtLibFeed data = extLib.getData(map);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        assertThat("ExtLibFeed entries size", data.getEntries(), hasSize(2));
        Entry entry = data.getEntries().get(1);
        checkSyndEntry(syndEntry, entry);

        assertThat("Entry other link size", entry.getOtherLinks(), hasSize(1));
        Link link = entry.getOtherLinks().get(0);
        assertEquals("Link Href", ExtLib.mapToUri("download?type=fb2&", syndLink.getHref()), link.getHref());
        assertEquals("Link Rel", syndLink.getRel(), link.getRel());

        assertThat("entry contents size", entry.getContents(), hasSize(1));
        Content content = entry.getContents().get(0);
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
        ExtLibFeed data = extLib.getData(map);
        assertEquals("ExtLibFeed Title", syndFeed.getTitle(), data.getTitle());

        assertThat("ExtLibFeed entries size", data.getEntries(), hasSize(2));
        Entry entry = data.getEntries().get(0);
        checkSyndEntry(syndEntry, entry);

        assertThat("Entry other link size", entry.getOtherLinks(), hasSize(1));
        Link link = entry.getOtherLinks().get(0);
        checkLync(syndLink, link);

        assertThat("entry contents size", entry.getContents(), hasSize(1));
        Content content = entry.getContents().get(0);
        checkContent(syndContent, content);

        Entry nextEntry = data.getEntries().get(1);
        assertEquals("Entry next Title", "Next", nextEntry.getTitle());

        Content nextContent = nextEntry.getContents().get(0);
        assertEquals("Content Type", "html", nextContent.getType());
        assertEquals("Content Value", "Next Page", nextContent.getValue());

        Link nextEntryLink = nextEntry.getOtherLinks().get(0);
        Link nextFeedLink = data.getLinks().get(0);
        checkLync(syndLinkNext, nextEntryLink);
        checkLync(syndLinkNext, nextFeedLink);

    }


    private void checkLync(SyndLinkImpl syndLink, Link link) {
        assertEquals("Link Href", ExtLib.mapToUri("?", syndLink.getHref()), link.getHref());
        assertEquals("Link Rel", syndLink.getRel(), link.getRel());
    }

    private void checkContent(SyndContentImpl syndContent, Content content) {
        assertEquals("Content Type", syndContent.getType(), content.getType());
        assertEquals("Content Value", syndContent.getValue(), content.getValue());
        assertEquals("Content Mode", syndContent.getMode(), content.getMode());
    }

    private void checkSyndEntry(SyndEntryImpl syndEntry, Entry entry) {
        assertEquals("Entry Title", syndEntry.getTitle(), entry.getTitle());
        assertEquals("Entry Id", syndEntry.getUri(), entry.getId());
    }
}
