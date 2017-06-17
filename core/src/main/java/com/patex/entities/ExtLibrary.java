package com.patex.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Entity
public class ExtLibrary {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String url;

    private String opdsPath;

    @Column(nullable = false)
    private String name;

    private String login;

    private String password;

    private String proxyHost;

    private Integer proxyPort;

    @Enumerated(EnumType.STRING)
    private Proxy.Type proxyType;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "extLibrary")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "extLibrary")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<SavedBook> savedBooks = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpdsPath() {
        return opdsPath;
    }

    public void setOpdsPath(String opdsPath) {
        this.opdsPath = opdsPath;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public Proxy.Type getProxyType() {
        return proxyType;
    }

    public void setProxyType(Proxy.Type proxyType) {
        this.proxyType = proxyType;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<SavedBook> getSavedBooks() {
        return savedBooks;
    }

    public void setSavedBooks(List<SavedBook> savedBooks) {
        this.savedBooks = savedBooks;
    }

    public Subscription addSubscription(String uri, ZUser user) {
        Subscription subscription = new Subscription(this, uri, user);
        getSubscriptions().add(subscription);
        return subscription;
    }


    public void addSaved(String extId){
        getSavedBooks().add(new SavedBook(this, extId));
    }
}
