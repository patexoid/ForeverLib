package com.patex.zombie.opds.converter;

import com.patex.zombie.model.Author;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.Sequence;
import com.patex.zombie.model.SequenceBook;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.converter.ExpandedAuthorEntries;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.patex.zombie.opds.converter.EntryVerifier.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExpandedAuthorEntryTest {

    @Test
    public void testEmptyAuthor() {
        Author author = new Author();
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
        Author author = new Author();
        Book book1 = new Book();
        Book book2 = new Book();
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        book2.setCreated(createdLater);
        author.setBooksNoSequence(Arrays.asList(book1, book2));
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
        Author author = new Author();
        Sequence sequence = new Sequence();
        sequence.setName("sequence");
        Book book1 = new Book();
        SequenceBook bookSequence1 = new SequenceBook(1, book1);
        Book book2 = new Book();
        SequenceBook bookSequence2 = new SequenceBook(2, book2);
        sequence.setBooks(Arrays.asList(bookSequence1, bookSequence2));
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        book2.setCreated(createdLater);
        author.setSequences(Collections.singletonList(sequence));

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
