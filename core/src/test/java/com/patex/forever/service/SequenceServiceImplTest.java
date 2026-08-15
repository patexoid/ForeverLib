package com.patex.forever.service;

import com.patex.forever.LibException;
import com.patex.forever.entities.BookEntity;
import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.BookSequenceRepository;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.entities.SequenceRepository;
import com.patex.forever.mapper.AuthorBookDataMapper;
import com.patex.forever.mapper.SequenceMapper;
import com.patex.forever.model.Sequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SequenceServiceImplTest {

    @Mock
    private SequenceRepository sequenceRepository;

    @Mock
    private BookSequenceRepository bookSequenceRepository;

    @Mock
    private SequenceMapper mapper;

    @Mock
    private AuthorBookDataMapper authorBookDataMapper;

    private SequenceServiceImpl sequenceService;

    @BeforeEach
    void setUp() {
        sequenceService = new SequenceServiceImpl(sequenceRepository, bookSequenceRepository, mapper,
                authorBookDataMapper, new TransactionService());
    }

    private static BookEntity book(long id) {
        BookEntity book = new BookEntity();
        book.setId(id);
        return book;
    }

    @Test
    void shouldReturnPagedSequencesFilteredByPrefix() {
        Pageable pageable = PageRequest.of(0, 10);
        SequenceEntity entity = new SequenceEntity(1L, "Foundation");
        Page<SequenceEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(sequenceRepository.getSequencesByName(pageable, "Found")).thenReturn(page);
        Sequence dto = new Sequence();
        dto.setId(1L);
        dto.setName("Foundation");
        when(mapper.toListDto(entity)).thenReturn(dto);

        Page<Sequence> result = sequenceService.getSequences(pageable, "Found");

        assertEquals(1, result.getTotalElements());
        assertEquals("Foundation", result.getContent().get(0).getName());
    }

    @Test
    void shouldSetBookSeqOrderAllowingDuplicates() {
        SequenceEntity entity = new SequenceEntity(1L, "Sequence");
        BookSequenceEntity bs1 = new BookSequenceEntity(0, entity, book(1L));
        BookSequenceEntity bs2 = new BookSequenceEntity(1, entity, book(2L));
        when(bookSequenceRepository.findBySequenceIdAndBookId(1L, 2L)).thenReturn(Optional.of(bs2));

        sequenceService.setBookOrder(1L, 2L, 0);

        assertEquals(0, bs1.getSeqOrder());
        assertEquals(0, bs2.getSeqOrder());
    }

    @Test
    void shouldRejectSettingOrderForBookNotInSequence() {
        when(bookSequenceRepository.findBySequenceIdAndBookId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(LibException.class, () -> sequenceService.setBookOrder(1L, 99L, 0));
    }

    @Test
    void shouldDeleteSequenceWithoutDeletingBooks() {
        SequenceEntity entity = new SequenceEntity(1L, "Sequence");
        BookSequenceEntity bs1 = new BookSequenceEntity(0, entity, book(1L));
        entity.setBookSequences(new ArrayList<>(List.of(bs1)));
        when(sequenceRepository.findById(1L)).thenReturn(Optional.of(entity));

        sequenceService.deleteSequence(1L);

        verify(sequenceRepository).delete(entity);
        verify(mapper, never()).toDto(any(SequenceEntity.class));
    }

    @Test
    void shouldMergeSequencesAndReassignBookSequenceRows() {
        SequenceEntity main = new SequenceEntity(1L, "Main");
        SequenceEntity secondary = new SequenceEntity(2L, "Secondary");
        BookSequenceEntity mainBook = new BookSequenceEntity(0, main, book(1L));
        BookSequenceEntity secondaryBook = new BookSequenceEntity(0, secondary, book(2L));
        main.setBookSequences(new ArrayList<>(List.of(mainBook)));
        secondary.setBookSequences(new ArrayList<>(List.of(secondaryBook)));
        when(sequenceRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(main, secondary));
        when(mapper.toDto(main)).thenReturn(new Sequence());

        Sequence mainDto = new Sequence();
        mainDto.setId(1L);
        Sequence secondaryDto = new Sequence();
        secondaryDto.setId(2L);

        sequenceService.mergeSequences(List.of(mainDto, secondaryDto));

        assertEquals(main, secondaryBook.getSequence());
        assertEquals(2, main.getBookSequences().size());
        verify(sequenceRepository).delete(secondary);
    }
}
