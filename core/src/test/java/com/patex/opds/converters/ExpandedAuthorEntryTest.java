package com.patex.opds.converters;

import com.patex.entities.*;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.patex.opds.converters.EntryVerifier.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

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
        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        assertThat(entries, hasSize(4));
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
        author.setBooks(Arrays.asList(new AuthorBook(author, book1), new AuthorBook(author, book2)));

        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        verifyDate(Date.from(createdLater), entries.get(0));
        verifyDate(Date.from(createdLater), entries.get(1));
        verifyDate(null, entries.get(2));
        verifyDate(Date.from(createdLater), entries.get(3));
        verifyNumberInContent(author.getBooks().size(), entries.get(1));
        verifyNumberInContent(0, entries.get(2));
        verifyNumberInContent(author.getBooks().size(), entries.get(3));
    }

    @Test
    public void testAuthorWithBookWithSequence() {
        Author author = new Author();
        Sequence sequence = new Sequence("sequence");
        Book book1 = new Book();
        BookSequence bookSequence1 = new BookSequence(1, sequence, book1);
        book1.setSequences(Collections.singletonList(bookSequence1));
        Book book2 = new Book();
        BookSequence bookSequence2 = new BookSequence(2, sequence, book2);
        book2.setSequences(Collections.singletonList(bookSequence2));
        sequence.setBookSequences(Arrays.asList(bookSequence1,bookSequence2));
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        book2.setCreated(createdLater);
        author.setBooks(Arrays.asList(new AuthorBook(author, book1), new AuthorBook(author, book2)));

        List<OPDSEntry> entries = new ExpandedAuthorEntries(author).getEntries();
        verifyDate(Date.from(createdLater), entries.get(0));
        verifyDate(Date.from(createdLater), entries.get(1));
        verifyDate(Date.from(createdLater), entries.get(2));
        verifyDate(null, entries.get(3));
        verifyNumberInContent(author.getBooks().size(), entries.get(1));
        verifyNumberInContent(author.getBooks().size(), entries.get(2));
        verifyNumberInContent(0, entries.get(3));
    }
}
