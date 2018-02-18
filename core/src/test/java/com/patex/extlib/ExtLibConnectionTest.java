package com.patex.extlib;

import com.google.common.util.concurrent.MoreExecutors;
import com.patex.entities.Book;
import com.patex.entities.ZUser;
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
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collections;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ExtLibConnectionTest {


    private RandomStringGenerator rsg = new RandomStringGenerator.Builder()
            .withinRange('a', 'z').build();

    @Test
    public void testDownloadBook() throws Exception {
        String fileName = rsg.generate(10);
        String url = "http://" + rsg.generate(10);
        String uri = rsg.generate(10);
        ZUser user = new ZUser();
        Book book = new Book();
        book.setId(RandomUtils.nextLong(0, 1000));
        String type = rsg.generate(10);
        InputStream is = mock(InputStream.class);

        BookService bookService = mock(BookService.class);
        when(bookService.uploadBook(fileName, is, user)).thenReturn(book);
        URLConnection urlConnection = mock(URLConnection.class);
        when(urlConnection.getInputStream()).thenReturn(is);
        when(urlConnection.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"" + fileName + "\"");

        ExtLibConnection connectionService = spy(new ExtLibConnection(url, "", null, null, null, 0, null,
                MoreExecutors.newDirectExecutorService(), bookService));
        when(connectionService.getConnection(url + uri)).thenReturn(urlConnection);
        Book actual = connectionService.downloadBook(uri, type, user);

        assertEquals(book.getId(), actual.getId());
    }

    @Test
    public void testGetDataSimpleFeed() throws Exception {
        String url = "http://" + rsg.generate(10);
        String uri = rsg.generate(10);

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setFeedType("atom_1.0");
        String title = rsg.generate(10);
        String entryUri = rsg.generate(10);
        String entryTitle = rsg.generate(10);
        String linkHref = rsg.generate(10);
        String contentValue = rsg.generate(10);

        syndFeed.setTitle(title);
        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setUri(entryUri);
        syndEntry.setTitle(entryTitle);

        SyndLink syndLink = new SyndLinkImpl();
        syndLink.setType("profile=opds-catalog");
        syndLink.setHref(linkHref);
        syndLink.setRel(rsg.generate(10));
        syndEntry.setLinks(Collections.singletonList(syndLink));

        SyndContent syndContent = new SyndContentImpl();
        syndContent.setType(rsg.generate(10));
        syndContent.setValue(contentValue);
        syndContent.setMode("xml");
        syndEntry.setContents(Collections.singletonList(syndContent));

        syndFeed.setEntries(Collections.singletonList(syndEntry));
        String expectedXML = new SyndFeedOutput().outputString(syndFeed);
        byte[] bytes = expectedXML.getBytes();

        ExtLibConnection connectionService = spy(new ExtLibConnection(url, "", null, null, null, 0, null,
                MoreExecutors.newDirectExecutorService(), mock(BookService.class)));
        URLConnection urlConnection = mock(URLConnection.class);
        when(connectionService.getConnection(url + uri)).thenReturn(urlConnection);
        when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));

        SyndFeed actualFeed = connectionService.getFeed(uri);
        assertEquals("ExtLibFeed Title", title, actualFeed.getTitle());

        assertThat(actualFeed.getEntries(), hasSize(1));
        SyndEntry actualEntry = actualFeed.getEntries().get(0);
        assertEquals("Entry URI", entryUri, actualEntry.getUri());
        assertEquals("Entry Title", entryTitle, actualEntry.getTitle());

        assertThat(actualEntry.getLinks(), hasSize(1));
        SyndLink actualLink = actualEntry.getLinks().get(0);
        assertEquals("Link href ", linkHref, actualLink.getHref());

        assertThat(actualEntry.getContents(), hasSize(1));
        SyndContent actualContent = actualEntry.getContents().get(0);
        assertEquals("Link href ", contentValue, actualContent.getValue());


    }

}
