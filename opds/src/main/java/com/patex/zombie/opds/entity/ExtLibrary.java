package com.patex.zombie.opds.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.Proxy;

/**
 *
 */
@Entity
@Getter
@Setter
public class ExtLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    private String opdsPath;

    @Column(nullable = false)
    private String name;

    private String login;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String proxyHost;

    private Integer proxyPort;

    @Enumerated(EnumType.STRING)
    private Proxy.Type proxyType;

    private String proxyUser;
    private String proxyPassword;

}
