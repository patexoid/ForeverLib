package com.patex.zombie.model;


import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class Author {

    private Long id;

    private String name;

    private List<Book> books = new ArrayList<>();

    private String descr;

    private Instant updated;

}