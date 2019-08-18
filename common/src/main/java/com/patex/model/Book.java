package com.patex.model;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class Book {

    private Long id;

    private List<BookAuthor> authors = new ArrayList<>();

    private List<BookSequence> sequences = new ArrayList<>();

    private List<Genre> genres = new ArrayList<>();

    private boolean duplicate = false;

    private String title;

    private String fileName;

    private Integer contentSize = 0;

    private Instant created;

    private byte[] checksum;

    private FileResource fileResource;

    private FileResource cover;

    private String descr;

    public Boolean isPrimary() {
        return !duplicate;
    }
}
