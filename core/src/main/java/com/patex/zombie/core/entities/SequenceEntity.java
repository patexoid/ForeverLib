package com.patex.zombie.core.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "SEQUENCE")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = SequenceEntity.class)
public class SequenceEntity {

    static final String BOOK_SEQUENCES = "books";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty
    private String name;

    @Transient
    private Instant updated;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy = "sequence")
    @JsonProperty(BOOK_SEQUENCES)
    private List<BookSequenceEntity> books = new ArrayList<>();


    public SequenceEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SequenceEntity(String name) {
        this.name = name;
    }

    public SequenceEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BookSequenceEntity> getBooks() {
        return books;
    }

    public void setBooks(List<BookSequenceEntity> books) {
        this.books = books;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }
}
