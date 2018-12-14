package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.patex.utils.BooleanJson;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @JsonSerialize(using = BooleanJson.Serializer.class)
    @JsonDeserialize(using = BooleanJson.Deserializer.class)
    private boolean duplicate = false;

    @Column(nullable = false)
    @JsonProperty
    private String title;

    @Column(nullable = false)
    @JsonProperty
    private String fileName;

    @JsonProperty
    private Integer contentSize = 0;

    @JsonIgnore
    private Instant created;

    @Column(nullable = false, updatable = false)
    @JsonIgnore
    private byte[] checksum;

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private FileResource fileResource;

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private FileResource cover;

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

    @JsonIgnore
    public List<Author> getAuthors() {
        return authorBooks.stream().map(AuthorBook::getAuthor).collect(Collectors.toList());
    }

    @JsonIgnore
    public void setAuthors(Collection<Author> authors) {
        List<AuthorBook> authorBooks = authors.stream().
                map(author -> new AuthorBook(author, this)).
                collect(Collectors.toList());
        setAuthorBooks(authorBooks);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public FileResource getCover() {
        return cover;
    }

    public void setCover(FileResource cover) {
        this.cover = cover;
    }

    @PostUpdate
    public void postUpdate() {
        getAuthorBooks().stream().map(AuthorBook::getAuthor).forEach(author -> author.setUpdated(getCreated()));
//        getSequences().stream().map(BookSequence::getSequence).forEach(sequence -> sequence.setUpdated(getCreated()));
    }
}
