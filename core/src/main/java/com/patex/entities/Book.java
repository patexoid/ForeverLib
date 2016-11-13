package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey on 11.03.2016.
 */
@Entity
public class Book {


    static final String AUTHORS_BOOKS = "authorBooks";
    static final String SEQUENCES = "sequences";
    static final String GENRES = "genres";
    static final String DESCR = "descr";

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(AUTHORS_BOOKS)
    private List<AuthorBook> authorBooks = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(SEQUENCES)
    private List<BookSequence> sequences = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST,}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(GENRES)
    private List<BookGenre> genres = new ArrayList<>();

    @Column(nullable = false)
    @JsonProperty
    private String title;

    @Column(nullable = false)
    @JsonProperty
    private String fileName;

    @Column(nullable = false)
    @JsonProperty
    private Integer size;

    @Column(nullable = false, updatable = false)
    @JsonIgnore
    private byte[] checksum;

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private FileResource fileResource;

    @Lob
    @JsonProperty(DESCR)
    private String descr;

    public Book() {
    }

    public Book(Author author, String name) {
        this.authorBooks.add(new AuthorBook(author, this));
        this.title = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addAuthor(Author author) {
        authorBooks.add(new AuthorBook(author, this));
    }

    public List<AuthorBook> getAuthorBooks() {
        return authorBooks;
    }

    public void setAuthorBooks(List<AuthorBook> authors) {
        this.authorBooks = authors;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public FileResource getFileResource() {
        return fileResource;
    }

    public void setFileResource(FileResource fileResource) {
        this.fileResource = fileResource;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public List<BookSequence> getSequences() {
        return sequences;
    }

    public void setSequences(List<BookSequence> sequences) {
        this.sequences = sequences;
    }

    public List<BookGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<BookGenre> genres) {
        this.genres = genres;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }
}
