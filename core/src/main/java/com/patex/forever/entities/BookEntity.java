package com.patex.forever.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.patex.forever.utils.BooleanJson;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Entity
@Table(name = "BOOK")
public class BookEntity {


    static final String AUTHORS_BOOKS = "authorBooks";
    static final String SEQUENCES = "sequences";
    static final String GENRES = "genres";
    static final String DESCR = "descr";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(AUTHORS_BOOKS)
    private List<AuthorBookEntity> authorBooks = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(SEQUENCES)
    private List<BookSequenceEntity> sequences = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "book")
    @JsonProperty(GENRES)
    private List<BookGenreEntity> genres = new ArrayList<>();

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

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private FileResourceEntity fileResource;

    @OneToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private FileResourceEntity cover;

    @Lob
    @JsonProperty(DESCR)
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String descr;

    @Column
    private String lang;
    @Column
    private String srcLang;
    @Column()
    private String langFb2;

    public BookEntity() {
    }

    public BookEntity(AuthorEntity author, String name) {
        this.authorBooks.add(new AuthorBookEntity(author, this));
        this.title = name;
    }

    public String getLangFb2() {
        return langFb2;
    }

    public void setLangFb2(String langFb2) {
        this.langFb2 = langFb2;
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

    public void addAuthor(AuthorEntity author) {
        authorBooks.add(new AuthorBookEntity(author, this));
    }

    public List<AuthorBookEntity> getAuthorBooks() {
        return authorBooks;
    }

    public void setAuthorBooks(List<AuthorBookEntity> authors) {
        this.authorBooks = authors;
    }

    @JsonIgnore
    public List<AuthorEntity> getAuthors() {
        return authorBooks.stream().map(AuthorBookEntity::getAuthor).collect(Collectors.toList());
    }

    @JsonIgnore
    public void setAuthors(Collection<AuthorEntity> authors) {
        List<AuthorBookEntity> authorBooks = authors.stream().
                map(author -> new AuthorBookEntity(author, this)).
                collect(Collectors.toList());
        setAuthorBooks(authorBooks);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public FileResourceEntity getFileResource() {
        return fileResource;
    }

    public void setFileResource(FileResourceEntity fileResource) {
        this.fileResource = fileResource;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public List<BookSequenceEntity> getSequences() {
        return sequences;
    }

    public void setSequences(List<BookSequenceEntity> sequences) {
        this.sequences = sequences;
    }

    public List<BookGenreEntity> getGenres() {
        return genres;
    }

    public void setGenres(List<BookGenreEntity> genres) {
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

    public FileResourceEntity getCover() {
        return cover;
    }

    public void setCover(FileResourceEntity cover) {
        this.cover = cover;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public void setSrcLang(String srcLang) {
        this.srcLang = srcLang;
    }

    @PostUpdate
    public void postUpdate() {
        getAuthorBooks().stream().map(AuthorBookEntity::getAuthor).forEach(author -> author.setUpdated(getCreated()));
//        getSequences().stream().map(BookSequence::getSequence).forEach(sequence -> sequence.setUpdated(getCreated()));
    }
}
