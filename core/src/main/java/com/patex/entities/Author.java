package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class Author {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "author")
    private List<AuthorBook> books = new ArrayList<>();

    @Lob
    private String descr;

    public Author() {
    }

    public Author(Long id,String name) {
this.id=id;
        this.name = name;
    }


    public Author(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AuthorBook> getBooks() {
        return books;
    }

    public void setBooks(List<AuthorBook> books) {
        this.books = books;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    @JsonIgnore
    public Stream<Sequence> getSequencesStream() {
        return getBooks().stream().
                flatMap(book -> book.getBook().getSequences().stream()).
                map(BookSequence::getSequence).
                distinct();
    }

    @JsonGetter
    public List<Sequence> getSequences() {
        return getSequencesStream().collect(Collectors.toList());
    }

    @JsonGetter
    public List<AuthorBook> getBooksNoSequence() {
        return getBooks().stream().filter(book -> book.getBook().getSequences().isEmpty()).collect(Collectors.toList());
    }


}
