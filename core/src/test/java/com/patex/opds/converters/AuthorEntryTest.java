package com.patex.opds.converters;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

public class AuthorEntryTest {

    @Test
    public void testId() {
        Author author = new Author();
        long id = 42L;
        author.setId(id);
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyId(""+id, entry);

    }

    @Test
    public void testName() {
        Author author = new Author();
        String name = "name";
        author.setName(name);
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyName(name, entry);
    }

    @Test
    public void testContent() {
        Author author = new Author();
        String content = "blah\nblahh";
        author.setDescr(content);
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyContent(entry, content);
    }

    @Test
    public void testEmptyContent() {
        Author author = new Author();
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyContent(entry);
    }

    @Test
    public void testDate() {
        Author author = new Author();
        Book book1 = new Book();
        Book book2 = new Book();
        Instant created = Instant.now().minus(30, ChronoUnit.DAYS);
        book1.setCreated(created);
        Instant createdLater = Instant.now();
        book2.setCreated(createdLater);
        author.setBooks(Arrays.asList(new AuthorBook(author, book1), new AuthorBook(author, book2)));

        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyDate(Date.from(createdLater), entry);
    }

}
