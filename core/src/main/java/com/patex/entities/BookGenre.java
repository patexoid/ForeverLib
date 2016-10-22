package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by Alexey on 11.05.2016.
 */
@Entity
public class BookGenre {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST},optional = false)
    @JsonIgnore
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST}, optional = false)
    private Genre genre;


    public BookGenre(Book book, Genre genre) {
        this.book = book;
        this.genre = genre;
    }

    public BookGenre() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
}
