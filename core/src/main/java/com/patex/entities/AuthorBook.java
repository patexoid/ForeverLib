package com.patex.entities;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;

/**
 * Created by Alexey on 10/22/2016.
 */
@Entity
public class AuthorBook {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST}, optional = false)
    @JsonIgnoreProperties({Author.SEQUENCES,Author.BOOKS_NO_SEQUENCE})
    private Author author;

    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST}, optional = false)
    @JsonIgnoreProperties({Book.AUTHORS_BOOKS,Book.SEQUENCES,Book.GENRES, Book.DESCR})
    private Book book;

    public AuthorBook() {

    }

    public AuthorBook(Author author, Book book) {
        this.author = author;
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
