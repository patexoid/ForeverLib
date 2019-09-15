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
    private BookEntity book;


//    @ManyToOne(fetch = FetchType.EAGER, optional = false)
//    private UserEntity user;
//

    public BookCheckQueue() {
    }

    public BookCheckQueue(BookEntity book) {
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public UserEntity getUser() {
//        return user;
//    }

//    public void setUser(UserEntity user) {
//        this.user = user;
//    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }
}
