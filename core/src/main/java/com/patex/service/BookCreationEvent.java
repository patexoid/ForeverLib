package com.patex.service;

import com.patex.entities.BookEntity;
import com.patex.model.User;

public class BookCreationEvent {
    private final BookEntity book;

    private final User user;

    public BookCreationEvent(BookEntity book, User user) {
        this.book = book;
        this.user = user;
    }

    public BookEntity getBook() {
        return book;
    }

    public User getUser() {
        return user;
    }
}
