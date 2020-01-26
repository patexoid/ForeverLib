package com.patex.zombie.core.entities;

import org.junit.Test;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

public class AuthorTest {

    @Test
    public void getSequences() {
        AuthorEntity author = new AuthorEntity("author");
        assertThat(author.getSequencesStream().collect(Collectors.toList()), is(empty()));
        BookEntity book1 = new BookEntity(author, "book1");
        author.getBooks().add(new AuthorBookEntity(author, book1));
        BookEntity book2 = new BookEntity(author, "book2");
        author.getBooks().add(new AuthorBookEntity(author, book2));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), is(empty()));
        SequenceEntity sequence1 = new SequenceEntity(1L,"sequence1");
        book1.getSequences().add(new BookSequenceEntity(0, sequence1));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(1));
        book1.getSequences().add(new BookSequenceEntity(0, sequence1));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(1));
        SequenceEntity sequence2 = new SequenceEntity(2L,"sequence2");
        BookEntity book3 = new BookEntity(author, "book3");
        author.getBooks().add(new AuthorBookEntity(author, book3));
        book3.getSequences().add(new BookSequenceEntity(0, sequence2));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(2));
        book2.getSequences().add(new BookSequenceEntity(0, sequence2));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(2));
    }
}
