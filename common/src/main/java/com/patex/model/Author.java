package com.patex.model;

import com.patex.zombie.core.utils.Tuple;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Author {

    private Long id;

    private String name;

    private List<Book> books = new ArrayList<>();

    private String descr;

    private Instant updated;

    public List<Sequence> getSequences() {
        return books.stream().
                flatMap(book -> book.getSequences().stream().map(bs -> new Tuple<>(book, bs))).
                collect(Collectors.toMap(t -> t._2().getSequenceName(), this::createSequence, this::mergeSequences)).
                values().stream().
                sorted(Comparator.comparing(Sequence::getName)).collect(Collectors.toList());
    }

    private Sequence mergeSequences(Sequence o, Sequence o2) {
        Sequence sequence = new Sequence();
        sequence.setId(o.getId());
        sequence.setName(o.getName());
        ArrayList<SequenceBook> books = new ArrayList<>(o.getBooks());
        books.addAll(o2.getBooks());
        books.sort(Comparator.comparing(SequenceBook::getSeqOrder));
        sequence.setBooks(books);
        return sequence;
    }

    private Sequence createSequence(Tuple<Book, BookSequence> t) {
        Sequence sequence = new Sequence();
        sequence.setId(t._2().getId());
        sequence.setName(t._2.getSequenceName());
        sequence.setBooks(Collections.singletonList(new SequenceBook(t._2().getSeqOrder(),t._1())));
        return sequence;
    }

    public List<Book> getBooksNoSequence() {
        return getBooks().stream().filter(book -> book.getSequences().isEmpty()).collect(Collectors.toList());
    }
}
