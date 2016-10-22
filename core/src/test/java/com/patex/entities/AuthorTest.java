package com.patex.entities;

import org.junit.Test;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

public class AuthorTest {

    @Test
    public void getSequences() {
        Author author = new Author("author");
        assertThat(author.getSequencesStream().collect(Collectors.toList()), is((empty())));
        Book book1 = new Book(author, "book1");
        author.getBooks().add(new AuthorBook(author, book1));
        Book book2 = new Book(author, "book2");
        author.getBooks().add(new AuthorBook(author, book2));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), is((empty())));
        Sequence sequence1 = new Sequence("sequence1");
        book1.getSequences().add(new BookSequence(0, sequence1));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(1));
        book1.getSequences().add(new BookSequence(0, sequence1));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(1));
        Sequence sequence2 = new Sequence("sequence2");
        Book book3 = new Book(author, "book3");
        author.getBooks().add(new AuthorBook(author, book3));
        book3.getSequences().add(new BookSequence(0, sequence2));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(2));
        book2.getSequences().add(new BookSequence(0, sequence2));
        assertThat(author.getSequencesStream().collect(Collectors.toList()), hasSize(2));

    }

}
