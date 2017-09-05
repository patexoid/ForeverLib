package com.patex.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 */
@Entity
public class BookCheckQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Book book1;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Book book2;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ZUser user;


    public BookCheckQueue() {
    }

    public BookCheckQueue(Book book1, Book book2, ZUser user) {
        this.book1 = book1;
        this.book2 = book2;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook1() {
        return book1;
    }

    public void setBook1(Book book1) {
        this.book1 = book1;
    }

    public Book getBook2() {
        return book2;
    }

    public void setBook2(Book book2) {
        this.book2 = book2;
    }

    public ZUser getUser() {
        return user;
    }

    public void setUser(ZUser user) {
        this.user = user;
    }
}
