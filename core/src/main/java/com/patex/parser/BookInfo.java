package com.patex.parser;

import com.patex.entities.BookEntity;
import com.patex.zombie.model.BookImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookInfo {

    private BookEntity book;
    private String coverage;
    private BookImage bookImage;
}
