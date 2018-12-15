package com.patex.extlib;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ZUser;
import com.patex.opds.OPDSContent;
import com.patex.opds.converters.OPDSAuthor;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSLink;
import com.patex.service.BookService;
import com.patex.utils.ExecutorCreator;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.feed.synd.SyndPersonImpl;
import com.rometools.rome.io.SyndFeedOutput;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Date;

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
    public static final Date UPDATED_DATE = new Date(12345000);
    public static final String AUTHOR_NAME = "authorName";

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

        ExecutorCreator executorCreator = mock(ExecutorCreator.class);
        when(executorCreator.createExecutor(any(), any())).thenReturn(MoreExecutors.newDirectExecutorService());
        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                executorCreator, bookService, 300));
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
        ExecutorCreator executorCreator = mock(ExecutorCreator.class);
        when(executorCreator.createExecutor(any(), any())).thenReturn(MoreExecutors.newDirectExecutorService());
        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                executorCreator, bookService, 300));
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
        syndEntry.setUpdatedDate(UPDATED_DATE);

        SyndPerson person = new SyndPersonImpl();
        person.setName(AUTHOR_NAME);
        syndEntry.setAuthors(Collections.singletonList(person));


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
        ExecutorCreator executorCreator = mock(ExecutorCreator.class);
        when(executorCreator.createExecutor(any(), any())).thenReturn(MoreExecutors.newDirectExecutorService());
        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                executorCreator, mock(BookService.class), 300));
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

        assertEquals(UPDATED_DATE.toInstant(), actualEntry.getUpdated());

        assertThat(actualEntry.getAuthors(), hasSize(1));
        OPDSAuthor author = actualEntry.getAuthors().get(0);
        assertEquals("author name", AUTHOR_NAME, author.getName());
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
        ExecutorCreator executorCreator = mock(ExecutorCreator.class);
        when(executorCreator.createExecutor(any(), any())).thenReturn(MoreExecutors.newDirectExecutorService());
        ExtLibConnection connectionService = spy(new ExtLibConnection(URL, "", null, null, null, 0, null,
                executorCreator, mock(BookService.class), 300));
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
