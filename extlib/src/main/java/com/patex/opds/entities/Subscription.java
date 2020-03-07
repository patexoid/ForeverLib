package com.patex.opds.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by Alexey on 07.05.2017.
 */
@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JsonIgnoreProperties
    private ExtLibrary extLibrary;

    private String link;

    @Column(nullable = false)
    @Getter
    @Setter
    private String userId;

    public Subscription() {

    }

    public Subscription(ExtLibrary extLibrary, String link, String userId) {
        this.extLibrary = extLibrary;
        this.link = link;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExtLibrary getExtLibrary() {
        return extLibrary;
    }

    public void setExtLibrary(ExtLibrary extLibrary) {
        this.extLibrary = extLibrary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


}
