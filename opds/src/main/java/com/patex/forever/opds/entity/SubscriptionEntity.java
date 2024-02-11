package com.patex.forever.opds.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Created by Alexey on 07.05.2017.
 */
@Entity
@Table(name = "SUBSCRIPTION")
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JsonIgnoreProperties
    private ExtLibrary extLibrary;

    private String link;

    @Column(name="USER_USERNAME")
    private String user;

    public SubscriptionEntity() {

    }

    public SubscriptionEntity(ExtLibrary extLibrary, String link, String user) {
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
