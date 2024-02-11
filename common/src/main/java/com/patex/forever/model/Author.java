package com.patex.forever.model;


import lombok.Data;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@Data
public class Author {

    private Long id;

    private String name;

    private List<Book> booksNoSequence = emptyList();

    private List<Sequence> sequences = emptyList();

    private String descr;

    private Instant updated;

    public List<Book> getBooks() {
        return Stream.concat(sequences.stream().map(Sequence::getBooks).
                        flatMap(Collection::stream).map(SequenceBook::getBook),
                booksNoSequence.stream()).collect(Collectors.toList());
    }
}