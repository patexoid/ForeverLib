package com.patex.forever.parser;

import com.patex.forever.entities.BookEntity;
import com.patex.forever.model.BookImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookInfo {

    private BookEntity book;
    private String coverage;
    private BookImage bookImage;
}
