package com.patex.zombie.opds.converter;

import com.patex.entities.BookEntity;
import com.patex.entities.FileResourceEntity;
import com.patex.zombie.opds.model.converter.BookEntry;
import org.junit.Test;

import java.time.Instant;

public class BookEntryTest {

    @Test
    public void testId() {
        BookEntity book = createBook();
        long id = 42L;
        book.setId(id);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyId("" + id, entry);
    }

    private BookEntity createBook() {
        BookEntity book = new BookEntity();
        book.setCreated(Instant.now());
        book.setFileResource(new FileResourceEntity("dsd", "sds", 42));
        return book;
    }

    @Test
    public void testName() {
        BookEntity book = createBook();
        String title = "name";
        book.setTitle(title);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyName(title, entry);
    }

    @Test
    public void testContent() {
        BookEntity book = createBook();
        String content = "blah\nblahh";
        book.setDescr(content);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyContent(entry, content);
    }

    @Test
    public void testDate() {
        BookEntity book = createBook();
        Instant created = Instant.now();
        book.setCreated(created);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyDate(created, entry);
    }
}
