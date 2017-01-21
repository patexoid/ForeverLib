package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Alexey on 11/26/2016.
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
}
