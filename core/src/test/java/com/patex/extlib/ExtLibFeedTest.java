package com.patex.extlib;

import com.patex.opds.OPDSEntryBuilder;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSLink;
import com.patex.utils.Res;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

public class ExtLibFeedTest {


    @Test
    public void testUpdateWithPrefix() {
        String entryLinkHref = "entryLink";
        String linkHref = "linkHref";
        String prefix = "linkPrefix.";

        final Object[] objects = new Object[]{};
        final Res bookTitle1 = new Res("bookTitle1", objects);
        OPDSEntry entry1 = new OPDSEntryBuilder("id1", Instant.now(), bookTitle1).
                addLink(entryLinkHref, "application/typ").build();
        OPDSLink link = new OPDSLink(linkHref, "typ");
        ExtLibFeed feed = new ExtLibFeed("title", Collections.singletonList(entry1),
                Collections.singletonList(link));
        ExtLibFeed feedWithPrefix = feed.updateWithPrefix(prefix);

        List<OPDSEntry> entriesWitPrefix = feedWithPrefix.getEntries();
        assertThat(entriesWitPrefix, hasSize(1));
        assertThat(feedWithPrefix.getLinks(), hasSize(1));
        List<OPDSLink> entryWithPrefixLinks = entriesWitPrefix.get(0).getLinks();
        assertThat(entryWithPrefixLinks, hasSize(1));

        assertEquals(prefix + entryLinkHref, entryWithPrefixLinks.get(0).getHref());
        assertEquals(prefix + linkHref, feedWithPrefix.getLinks().get(0).getHref());

    }
}


