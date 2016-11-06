package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JoinColumn(name = "author_ID")
    private Author author;

    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(name = "book_ID")
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

    @JsonGetter
    public String getTitle() {
        return book==null?null:book.getTitle();
    }

    @JsonGetter
    public String getName() {
        return author==null?null:author.getName();
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
