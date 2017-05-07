package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = Sequence.class)
public class Sequence {

    static final String BOOK_SEQUENCES = "bookSequences";

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    @JsonProperty
    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER, mappedBy = "sequence")
    @JsonProperty(BOOK_SEQUENCES)
    private List<BookSequence> bookSequences = new ArrayList<>();


    Sequence(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Sequence(String name) {
        this.name = name;
    }

    public Sequence() {
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

    public List<BookSequence> getBookSequences() {
        return bookSequences;
    }

    public void setBookSequences(List<BookSequence> bookSequences) {
        this.bookSequences = bookSequences;
    }
}
