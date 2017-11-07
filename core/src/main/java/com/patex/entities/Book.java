package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.patex.utils.BooleanJson;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 *
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

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(AUTHORS_BOOKS)
    private List<AuthorBook> authorBooks = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(SEQUENCES)
    private List<BookSequence> sequences = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(GENRES)
    private List<BookGenre> genres = new ArrayList<>();

    @JsonSerialize(using=BooleanJson.Serializer.class)
    @JsonDeserialize(using=BooleanJson.Deserializer.class)
    private boolean duplicate = false;

    @Column(nullable = false)
    @JsonProperty
    private String title;

    @Column(nullable = false)
    @JsonProperty
    private String fileName;

    @Column(nullable = false)
    @JsonProperty
    private Integer size;

    @JsonProperty
    private Integer contentSize;

    @JsonIgnore
    private Instant created;

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

    public Boolean isDuplicate() {
        return duplicate;
    }


    public Boolean isPrimary() {
        return !duplicate;
    }
    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Integer getContentSize() {
        return contentSize;
    }

    public void setContentSize(Integer contentSize) {
        this.contentSize = contentSize;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
