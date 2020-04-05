package com.patex.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class BookCheckQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BOOK_ID")
    private long book;

    @Column(name = "USER_USERNAME")
    private String user;

    public BookCheckQueue(long book, String user) {
        this.book = book;
        this.user = user;
    }
}
