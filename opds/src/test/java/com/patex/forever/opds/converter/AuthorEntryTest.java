package com.patex.forever.opds.converter;

import com.patex.forever.model.Author;
import com.patex.forever.opds.model.converter.AuthorEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class AuthorEntryTest {

    @Test
    public void testId() {
        Author author = new Author();
        long id = 42L;
        author.setId(id);
        author.setUpdated(Instant.now());

        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyId("" + id, entry);

    }

    @Test
    public void testName() {
        Author author = new Author();
        String name = "name";
        author.setName(name);
        author.setUpdated(Instant.now());
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyName(name, entry);
    }

    @Test
    public void testContent() {
        Author author = new Author();
        author.setUpdated(Instant.now());
        String content = "blah\nblahh";
        author.setDescr(content);
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyContent(entry, content);
    }

    @Test
    public void testEmptyContent() {
        Author author = new Author();
        author.setUpdated(Instant.now());
        AuthorEntry entry = new AuthorEntry(author);
        EntryVerifier.verifyContent(entry);
    }

}
