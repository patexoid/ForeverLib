package com.patex.forever.mapper;

import com.patex.forever.entities.AuthorBookEntity;
import com.patex.forever.entities.AuthorEntity;
import com.patex.forever.entities.BookEntity;
import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.model.Author;
import com.patex.forever.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Regression test for ADMIN_UI_DESIGN.md Task B1: {@code GET /author/{id}} (AuthorController.getAuthor
 * -> AuthorMapper.toDto) must keep returning fully-populated books in both {@code sequences} and
 * {@code booksNoSequence}, not the reduced projection {@code AuthorServiceImpl.getAuthorSimplified} uses.
 */
public class AuthorMapperTest {

    private AuthorMapper authorMapper;

    @BeforeEach
    public void setUp() {
        BookMapper bookMapper = new BookMapperImpl();

        SequenceMapper.SequenceBookMapper sequenceBookMapper = new SequenceMapper$SequenceBookMapperImpl();
        ReflectionTestUtils.setField(sequenceBookMapper, "bookMapper", bookMapper);

        SequenceMapper sequenceMapper = new SequenceMapperImpl();
        ReflectionTestUtils.setField(sequenceMapper, "sequenceBookMapper", sequenceBookMapper);

        authorMapper = new AuthorMapperImpl();
        ReflectionTestUtils.setField(authorMapper, "bookMapper", bookMapper);
        ReflectionTestUtils.setField(authorMapper, "sequenceMapper", sequenceMapper);
    }

    @Test
    public void shouldPopulateSequencesAndBooksNoSequenceWithFullBookData() {
        AuthorEntity author = new AuthorEntity(1L, "author");

        BookEntity bookInSequence = new BookEntity(author, "book in sequence");
        bookInSequence.setFileName("in-sequence.fb2");
        bookInSequence.setContentSize(1234);
        bookInSequence.setLang("ru");
        SequenceEntity sequence = new SequenceEntity(2L, "sequence");
        BookSequenceEntity bookSequenceEntity = new BookSequenceEntity(0, sequence, bookInSequence);
        bookInSequence.setSequences(List.of(bookSequenceEntity));
        sequence.setBookSequences(List.of(bookSequenceEntity));

        BookEntity bookWithoutSequence = new BookEntity(author, "standalone book");
        bookWithoutSequence.setFileName("standalone.fb2");
        bookWithoutSequence.setContentSize(5678);
        bookWithoutSequence.setLang("en");

        author.setBooks(List.of(
                new AuthorBookEntity(author, bookInSequence),
                new AuthorBookEntity(author, bookWithoutSequence)));

        Author dto = authorMapper.toDto(author);

        assertFalse(dto.getSequences().isEmpty(), "sequences should not be empty");
        assertFalse(dto.getBooksNoSequence().isEmpty(), "booksNoSequence should not be empty");

        Book sampledSequenceBook = dto.getSequences().get(0).getBooks().get(0).getBook();
        assertEquals("in-sequence.fb2", sampledSequenceBook.getFileName());
        assertEquals(1234, sampledSequenceBook.getContentSize().intValue());
        assertEquals("ru", sampledSequenceBook.getLang());

        Book sampledStandaloneBook = dto.getBooksNoSequence().get(0);
        assertEquals("standalone.fb2", sampledStandaloneBook.getFileName());
        assertEquals(5678, sampledStandaloneBook.getContentSize().intValue());
        assertEquals("en", sampledStandaloneBook.getLang());
    }
}
