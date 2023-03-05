package com.patex.zombie.opds.converter;

import com.patex.zombie.opds.model.ODPSContentRes;
import com.patex.zombie.opds.model.OPDSContent;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntryVerifier {

    static void verifyId(String id, OPDSEntry entry) {
        assertTrue(entry.getId().contains(id));
        entry.getLinks().stream().map(OPDSLink::getHref).forEach(
                s -> MatcherAssert.assertThat(s, Matchers.containsString(id))
        );
    }

    static void verifyName(String name, OPDSEntry entry) {
        assertTrue(Arrays.asList(entry.getTitle().getObjs()).contains(name));
        assertEquals(name, entry.getTitle().getObjs()[0]);
    }

    static void verifyContent(OPDSEntry entry, String... stringContent) {
        List<OPDSContent> contents = entry.getContent();
        assertThat(contents, IsCollectionWithSize.hasSize(stringContent.length));
        for (int i = 0; i < stringContent.length; i++) {
            assertEquals(stringContent[i], contents.get(i).getValue());
        }
    }

    static void verifyDate(Instant expectedDate, OPDSEntry entry) {
        assertEquals(expectedDate, entry.getUpdated());
    }

    static void verifyNumberInContent(Integer number, OPDSEntry entry) {
        boolean containsINContent = entry.getContent().stream().
                map(c -> (ODPSContentRes) c).
                flatMap(c -> Stream.of(c.getObjs())).
                anyMatch(number::equals);
        assertTrue(containsINContent, number + "is not in content");
    }
}
