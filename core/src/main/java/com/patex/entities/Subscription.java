package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @ManyToOne(optional = false)
    private ZUser user;

    public Subscription() {

    }

    public Subscription(ExtLibrary extLibrary, String link, ZUser user) {
        this.extLibrary = extLibrary;
        this.link = link;
        this.user = user;
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

    public ZUser getUser() {
        return user;
    }

    public void setUser(ZUser user) {
        this.user = user;
    }


}
