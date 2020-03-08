package com.patex.zombie.opds.converter;

import com.patex.entities.*;
import com.patex.zombie.opds.model.converter.ExpandedAuthorEntries;
import com.patex.zombie.opds.model.OPDSEntry;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.patex.zombie.opds.converter.EntryVerifier.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class ExpandedAuthorEntryTest {

    @Test
    public void testEmptyAuthor() {
        AuthorEntity author = new AuthorEntity();
        long id = 42L;
        author.setId(id);
        String name = "name";
        author.setName(name);
        String descr = "descr";
        author.setDescr(descr);
        author.setUpdated(Instant.now());
        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        Assert.assertThat(entries, IsCollectionWithSize.hasSize(4));
        entries.forEach(entry -> verifyId("" + id, entry));
        entries.forEach(entry -> verifyName(name, entry));
        verifyContent(entries.get(0), descr);
    }


    @Test
    public void testAuthorWithBookNoSequence() {
        AuthorEntity author = new AuthorEntity();
        BookEntity book1 = new BookEntity();
        BookEntity book2 = new BookEntity();
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        book2.setCreated(createdLater);
        author.setBooks(Arrays.asList(new AuthorBookEntity(author, book1), new AuthorBookEntity(author, book2)));
        Instant authorUpdated = Instant.now();
        author.setUpdated(authorUpdated);
        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        verifyDate(authorUpdated, entries.get(0));
        verifyDate(authorUpdated, entries.get(1));
        verifyDate(createdLater, entries.get(3));
        verifyNumberInContent(author.getBooks().size(), entries.get(1));
        verifyNumberInContent(0, entries.get(2));
        verifyNumberInContent(author.getBooks().size(), entries.get(3));
    }

    @Test
    public void testAuthorWithBookWithSequence() {
        AuthorEntity author = new AuthorEntity();
        SequenceEntity sequence = new SequenceEntity("sequence");
        BookEntity book1 = new BookEntity();
        BookSequenceEntity bookSequence1 = new BookSequenceEntity(1, sequence, book1);
        book1.setSequences(Collections.singletonList(bookSequence1));
        BookEntity book2 = new BookEntity();
        BookSequenceEntity bookSequence2 = new BookSequenceEntity(2, sequence, book2);
        book2.setSequences(Collections.singletonList(bookSequence2));
        sequence.setBookSequences(Arrays.asList(bookSequence1, bookSequence2));
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        book2.setCreated(createdLater);
        author.setBooks(Arrays.asList(new AuthorBookEntity(author, book1), new AuthorBookEntity(author, book2)));

        Instant authorUpdated = Instant.now();
        author.setUpdated(authorUpdated);

        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        verifyDate(authorUpdated, entries.get(0));
        verifyDate(authorUpdated, entries.get(1));
        verifyNumberInContent(author.getBooks().size(), entries.get(1));
        verifyNumberInContent(author.getBooks().size(), entries.get(2));
        verifyNumberInContent(0, entries.get(3));
    }
}
