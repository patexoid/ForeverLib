package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by Alexey on 11.05.2016.
 */
@Entity
public class BookSequence {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private int seqOrder;

    @ManyToOne(cascade = {CascadeType.PERSIST})
    @JsonIgnore
    private Book book;

    @ManyToOne(cascade = {CascadeType.PERSIST})
    @JsonIgnore
    private Sequence sequence;


    public BookSequence(int order, Sequence sequence) {
        this.seqOrder = order;
        this.sequence = sequence;
    }

    public BookSequence() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public int getSeqOrder() {
        return seqOrder;
    }

    public void setSeqOrder(int order) {
        this.seqOrder = order;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
}
