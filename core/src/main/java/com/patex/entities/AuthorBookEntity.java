package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "AUTHOR_BOOK")
public class AuthorBookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST}, optional = false)
    @JsonIgnoreProperties({AuthorEntity.SEQUENCES, AuthorEntity.BOOKS_NO_SEQUENCE})
    private AuthorEntity author;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST}, optional = false)
    @JsonIgnoreProperties({BookEntity.AUTHORS_BOOKS, BookEntity.SEQUENCES, BookEntity.GENRES, BookEntity.DESCR})
    private BookEntity book;

    public AuthorBookEntity() {

    }

    public AuthorBookEntity(AuthorEntity author, BookEntity book) {
        this.author = author;
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuthorEntity getAuthor() {
        return author;
    }

    public void setAuthor(AuthorEntity author) {
        this.author = author;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }
}
