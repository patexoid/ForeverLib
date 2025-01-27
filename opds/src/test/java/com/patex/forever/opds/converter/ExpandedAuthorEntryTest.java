package com.patex.forever.opds.converter;

import com.patex.forever.model.Author;
import com.patex.forever.model.AuthorDescription;
import com.patex.forever.model.Book;
import com.patex.forever.model.Sequence;
import com.patex.forever.model.SequenceBook;
import com.patex.forever.opds.model.OPDSEntry;
import com.patex.forever.opds.model.converter.ExpandedAuthorEntries;
import lombok.Getter;
import lombok.Setter;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.patex.forever.opds.converter.EntryVerifier.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExpandedAuthorEntryTest {

    @Test
    public void testEmptyAuthor() {
        AuthorDescriptionImpl author = new AuthorDescriptionImpl();
        long id = 42L;
        author.setId(id);
        String name = "name";
        author.setName(name);
        String descr = "descr";
        author.setDescr(descr);
        author.setUpdated(Instant.now());
        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        assertThat(entries, IsCollectionWithSize.hasSize(4));
        entries.forEach(entry -> verifyId("" + id, entry));
        entries.forEach(entry -> verifyName(name, entry));
        verifyContent(entries.get(0), descr);
    }


    @Test
    public void testAuthorWithBookNoSequence() {
        AuthorDescriptionImpl author = new AuthorDescriptionImpl();
        Book book1 = new Book();
        Book book2 = new Book();
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        author.setNoSequenceUpdated(createdLater);
        author.setBookCount(2);
        author.setNoSequenceBookCount(2);
        Instant authorUpdated = Instant.now();
        author.setUpdated(authorUpdated);
        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        verifyDate(authorUpdated, entries.get(0));
        verifyDate(authorUpdated, entries.get(1));
        verifyDate(createdLater, entries.get(3));
        verifyNumberInContent(2, entries.get(1));
        verifyNumberInContent(0, entries.get(2));
        verifyNumberInContent(2, entries.get(3));
    }

    @Test
    public void testAuthorWithBookWithSequence() {
        AuthorDescriptionImpl author = new AuthorDescriptionImpl();
        author.setSequenceCount(1);
        author.setBookCount(2);
        author.setSequenceBookCount(2);
        Instant authorUpdated = Instant.now();
        author.setUpdated(authorUpdated);

        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        verifyDate(authorUpdated, entries.get(0));
        verifyDate(authorUpdated, entries.get(1));
        verifyNumberInContent(2, entries.get(1));
        verifyNumberInContent(2, entries.get(2));
        verifyNumberInContent(0, entries.get(3));
    }

    @Getter
    @Setter
    private static class AuthorDescriptionImpl implements AuthorDescription {
        private Long id;

        private String name;

        private String descr;

        private Instant updated;

        private int bookCount;

        private int sequenceCount;
        private int sequenceBookCount;
        Instant sequenceUpdated;

        private int noSequenceBookCount;
        private Instant noSequenceUpdated;
    }
}
