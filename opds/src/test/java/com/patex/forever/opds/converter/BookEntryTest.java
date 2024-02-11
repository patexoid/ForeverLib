package com.patex.forever.opds.converter;

import com.patex.forever.model.Book;
import com.patex.forever.model.FileResource;
import com.patex.forever.opds.model.converter.BookEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class BookEntryTest {

    @Test
    public void testId() {
        Book book = createBook();
        long id = 42L;
        book.setId(id);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyId("" + id, entry);
    }

    private Book createBook() {
        Book book = new Book();
        book.setCreated(Instant.now());
        book.setFileResource(new FileResource("dsd", "sds", 42));
        return book;
    }

    @Test
    public void testName() {
        Book book = createBook();
        String title = "name";
        book.setTitle(title);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyName(title, entry);
    }

    @Test
    public void testContent() {
        Book book = createBook();
        String content = "blah\nblahh";
        book.setDescr(content);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyContent(entry, content);
    }

    @Test
    public void testDate() {
        Book book = createBook();
        Instant created = Instant.now();
        book.setCreated(created);
        BookEntry entry = new BookEntry(book);
        EntryVerifier.verifyDate(created, entry);
    }
}
