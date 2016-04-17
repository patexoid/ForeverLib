package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Author {

    @Id
    @GeneratedValue
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "authors")
    @JsonIgnore
    private List<Book> books = new ArrayList<Book>();

    public Author() {
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

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
