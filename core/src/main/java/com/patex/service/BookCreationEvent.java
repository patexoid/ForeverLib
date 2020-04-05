package com.patex.service;

import com.patex.zombie.model.Book;
import com.patex.zombie.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BookCreationEvent {

    private final Book book;
    private final User user;
}
