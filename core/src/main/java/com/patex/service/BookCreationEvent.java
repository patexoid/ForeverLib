package com.patex.service;

import com.patex.entities.Book;
import com.patex.entities.ZUser;

public class BookCreationEvent  {
    private final Book book;

    private final ZUser user;

    public BookCreationEvent(Book book, ZUser user) {
        this.book = book;
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public ZUser getUser() {
        return user;
    }
}
