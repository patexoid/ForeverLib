package com.patex.service;

import com.patex.entities.BookEntity;
import com.patex.entities.UserEntity;

public class BookCreationEvent {
    private final BookEntity book;

    private final UserEntity user;

    public BookCreationEvent(BookEntity book, UserEntity user) {
        this.book = book;
        this.user = user;
    }

    public BookEntity getBook() {
        return book;
    }

    public UserEntity getUser() {
        return user;
    }
}
