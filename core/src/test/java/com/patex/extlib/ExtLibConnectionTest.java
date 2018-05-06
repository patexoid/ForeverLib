package com.patex.extlib;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ZUser;
import com.patex.opds.OPDSContent;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSLink;
import com.patex.service.BookService;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import com.rometools.rome.io.SyndFeedOutput;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collections;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExtLibConnectionTest {

    private static final String FILE_NAME = "filename";
    private static final String URL = "http://example.com";
    private static final String URI = "/path";
    private static final String TYPE = "type";
    private static final String TITLE = "title";
    private static final String ENTRY_URI = "entryPath";
    private static final String ENTRY_TITLE = "entryTitle";
    private static final String LINK_HREF = "linkHref";
    private static final String FEED_TYPE = "atom_1.0";
    private static final String CONTENT_VALUE = "contentValue";
    private static final String REL = "Rel";

    @Test
    public void testDownloadBook() throws Exception {
        ZUser user = new ZUser();
        Book book = new Book();
        book.setId(RandomUtils.nextLong(0, 1000));
        InputStream is = mock(InputStream.class);

        BookService bookService = mock(BookService.class);
        when(bookService.uploadBook(FILE_NAME, is, user)).thenReturn(book);
        URLConnection urlConnection = mock(URLConnection.class);
        when(urlConnection.getInputStream()).thenReturn(is);
        when(urlConnection.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + FILE_NAME + "\"");

        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                MoreExecutors.newDirectExecutorService(), bookService, 300));
        when(connectionService.getConnection(URL + URI)).thenReturn(urlConnection);
        Book actual = connectionService.downloadBook(URI, TYPE, user);

        assertEquals(book.getId(), actual.getId());
    }

    @Test(expected = LibException.class)
    public void testDownloadBookLibException() throws Exception {
        ZUser user = new ZUser();

        BookService bookService = mock(BookService.class);
        when(bookService.uploadBook(any(), any(), eq(user))).thenThrow(new LibException());
        URLConnection urlConnection = mock(URLConnection.class);
        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                MoreExecutors.newDirectExecutorService(), bookService, 300));
        when(connectionService.getConnection(URL + URI)).thenReturn(urlConnection);
        connectionService.downloadBook(URI, TYPE, user);
    }

    @Test
    public void testGetDataSimpleFeed() throws Exception {

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setFeedType(FEED_TYPE);
        syndFeed.setTitle(TITLE);
        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setUri(ENTRY_URI);
        syndEntry.setTitle(ENTRY_TITLE);

        SyndLink syndLink = new SyndLinkImpl();
        syndLink.setType("profile=opds-catalog");
        syndLink.setHref(LINK_HREF);
        syndLink.setRel(REL);
        syndEntry.setLinks(Collections.singletonList(syndLink));

        SyndContent syndContent = new SyndContentImpl();
        syndContent.setType(TYPE);
        syndContent.setValue(CONTENT_VALUE);
        syndContent.setMode("xml");
        syndEntry.setContents(Collections.singletonList(syndContent));

        syndFeed.setEntries(Collections.singletonList(syndEntry));
        String expectedXML = new SyndFeedOutput().outputString(syndFeed);
        byte[] bytes = expectedXML.getBytes();

        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                MoreExecutors.newDirectExecutorService(), mock(BookService.class), 300));
        URLConnection urlConnection = mock(URLConnection.class);
        when(connectionService.getConnection(URL + URI)).thenReturn(urlConnection);
        when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));

        ExtLibFeed actualFeed = connectionService.getFeed(URI);
        assertEquals("ExtLibFeed Title", TITLE, actualFeed.getTitle());

        assertThat(actualFeed.getEntries(), hasSize(1));
        OPDSEntry actualEntry = actualFeed.getEntries().get(0);
        assertEquals("Entry URI", ENTRY_URI, actualEntry.getId());
        assertEquals("Entry Title", ENTRY_TITLE, actualEntry.getTitle().getObjs()[0]);

        assertThat(actualEntry.getLinks(), hasSize(1));
        OPDSLink actualLink = actualEntry.getLinks().get(0);
        assertEquals("Link href ", "?uri=" + LINK_HREF, actualLink.getHref());

        assertThat(actualEntry.getContent(), hasSize(1));
        OPDSContent actualContent = actualEntry.getContent().get(0);
        assertEquals("Link href ", CONTENT_VALUE, actualContent.getValue());
    }


    @Test
    public void testGetDataSimpleFeedWithNextLink() throws Exception {
        String nextHref = "nextHref";
        String linkType = "profile=opds-catalog";

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setFeedType(FEED_TYPE);
        syndFeed.setTitle(TITLE);

        SyndLinkImpl syndLinkNext = new SyndLinkImpl();
        syndLinkNext.setRel(ExtLibService.REL_NEXT);
        syndLinkNext.setHref(nextHref);
        syndLinkNext.setType(linkType);
        syndFeed.setLinks(Collections.singletonList(syndLinkNext));

        String expectedXML = new SyndFeedOutput().outputString(syndFeed);
        byte[] bytes = expectedXML.getBytes();

        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                MoreExecutors.newDirectExecutorService(), mock(BookService.class), 300));
        URLConnection urlConnection = mock(URLConnection.class);
        when(connectionService.getConnection(URL + URI)).thenReturn(urlConnection);
        when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));

        ExtLibFeed actualFeed = connectionService.getFeed(URI);

        assertThat(actualFeed.getLinks(), hasSize(1));
        OPDSLink opdsLink = actualFeed.getLinks().get(0);
        assertEquals("Link href", "?uri=" + nextHref, opdsLink.getHref());
        assertEquals("Link rel", ExtLibService.REL_NEXT, opdsLink.getRel());
        assertEquals("Link type ", linkType, opdsLink.getType());
    }
}
