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
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = Book.class)
public class Book {


    @Id
    @GeneratedValue
    @JsonProperty
    private long id;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "book")
    private List<AuthorBook> authors = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "book")
    private List<BookSequence> sequences = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST,}, fetch = FetchType.LAZY, mappedBy = "book")
    private List<BookGenre> genres = new ArrayList<>();

    @Column(nullable = false)
    @JsonProperty
    private String title;

    @Column(nullable = false)
    @JsonProperty
    private String fileName;

    @Column(nullable = false)
    @JsonProperty
    private long size;

    @Column(nullable = false, updatable = false)
    @JsonIgnore
    private byte[] checksum;

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private FileResource fileResource;

    @Lob
    @JsonProperty
    private String descr;

    public Book() {
    }

    public Book(Author author, String name) {
        this.authors.add(new AuthorBook(author, this));
        this.title = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addAuthor(Author author) {
        authors.add(new AuthorBook(author, this));
    }

    public List<AuthorBook> getAuthorBooks() {
        return authors;
    }

    public void setAuthorBooks(List<AuthorBook> authors) {
        this.authors = authors;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
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
