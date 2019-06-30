package com.patex.lrequest.state;

import com.patex.entities.Author;
import com.patex.entities.Book;
import lombok.Data;

import java.util.List;

@Data
public class BooksState {
    private final String filter;
    private final int count;
    private final List<Book> books;

}
