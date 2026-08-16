package com.patex.forever.service;

import com.patex.forever.LibException;
import com.patex.forever.entities.AuthorBookEntity;
import com.patex.forever.entities.AuthorEntity;
import com.patex.forever.entities.AuthorRepository;
import com.patex.forever.entities.BookRepository;
import com.patex.forever.entities.SequenceRepository;
import com.patex.forever.mapper.AuthorBookDataMapper;
import com.patex.forever.mapper.AuthorMapper;
import com.patex.forever.mapper.AuthorMapperImpl;
import com.patex.forever.model.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceUnitTest {

    @Mock
    private AuthorRepository authorRepository;

    private final AuthorMapper mapper = new AuthorMapperImpl();

    @Mock
    private AuthorBookDataMapper authorBookDataMapper;

    @Mock
    private RabbitService rabbitService;

    @Mock
    private SequenceRepository sequenceRepository;

    @Mock
    private BookRepository bookRepository;

    private AuthorServiceImpl authorService;

    @BeforeEach
    void setUp() {
        authorService = new AuthorServiceImpl(authorRepository, mapper, authorBookDataMapper, rabbitService,
                new TransactionService(), sequenceRepository, bookRepository);
    }

    @Test
    void shouldUpdateAuthorBioAndKeepBooksUnchanged() {
        AuthorEntity entity = new AuthorEntity(1L, "Original Name");
        List<AuthorBookEntity> books = entity.getBooks();
        when(authorRepository.findById(1L)).thenReturn(Optional.of(entity));
        Author patch = new Author();
        patch.setName("Updated Name");
        patch.setDescr("Updated bio");

        Author result = authorService.updateAuthor(1L, patch);

        assertEquals("Updated Name", entity.getName());
        assertEquals("Updated bio", entity.getDescr());
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated bio", result.getDescr());
        assertEquals(books, entity.getBooks());
    }

    @Test
    void shouldRejectDeletingAuthorWithRemainingBooks() {
        AuthorEntity entity = new AuthorEntity(1L, "Author With Books");
        entity.setBooks(List.of(new AuthorBookEntity()));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(LibException.class, () -> authorService.deleteAuthor(1L));
        verify(authorRepository, never()).delete(entity);
    }
}
