package com.patex.service;

import com.patex.entities.BookEntity;
import com.patex.entities.ZUser;

public class BookCreationEvent  {
    private final BookEntity book;

    private final ZUser user;

    public BookCreationEvent(BookEntity book, ZUser user) {
        this.book = book;
        this.user = user;
    }

    public BookEntity getBook() {
        return book;
    }

    public ZUser getUser() {
        return user;
    }
}
