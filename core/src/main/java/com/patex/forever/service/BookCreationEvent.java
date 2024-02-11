package com.patex.forever.service;

import com.patex.forever.parser.BookInfo;
import com.patex.forever.model.Book;
import com.patex.forever.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BookCreationEvent {

    private final Book book;

    private final BookInfo bookInfo;
    private final User user;

}
