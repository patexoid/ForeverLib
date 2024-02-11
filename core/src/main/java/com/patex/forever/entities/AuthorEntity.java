package com.patex.forever.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.patex.forever.StreamU;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.*;

import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "AUTHOR")
public class AuthorEntity {

    static final String BOOKS_NO_SEQUENCE = "booksNoSequence";
    static final String SEQUENCES = "sequences";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "author")
    @JsonIgnore
    private List<AuthorBookEntity> books = new ArrayList<>();

    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String descr;

    private Instant updated;

    public AuthorEntity() {
    }

    public AuthorEntity(Long id, String name, Instant updated) {
        this.id = id;
        this.name = name;
        this.updated = updated;
    }

    public AuthorEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }


    public AuthorEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AuthorBookEntity> getBooks() {
        return books;
    }

    public void setBooks(List<AuthorBookEntity> books) {
        this.books = books;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    @JsonIgnore
    public Stream<SequenceEntity> getSequencesStream() {
        return getBooks().stream().
                flatMap(book -> book.getBook().getSequences().stream()).
                map(BookSequenceEntity::getSequence).
                filter(StreamU.distinctByKey(SequenceEntity::getId));
    }

    @JsonGetter(SEQUENCES)
    public List<SequenceEntity> getSequences() {
        return getSequencesStream().collect(Collectors.toList());
    }

    @JsonGetter(BOOKS_NO_SEQUENCE)
    public List<AuthorBookEntity> getBooksNoSequence() {
        return getBooks().stream().filter(book -> book.getBook().getSequences().isEmpty()).collect(Collectors.toList());
    }


    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }
}
