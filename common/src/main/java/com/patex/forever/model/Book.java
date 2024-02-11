package com.patex.forever.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class Book extends SimpleBook {

    private List<BookAuthor> authors = new ArrayList<>();

    private List<BookSequence> sequences = new ArrayList<>();

    private List<Genre> genres = new ArrayList<>();


    private FileResource cover;

}


