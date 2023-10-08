package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.*;

/**
 * Created by Alexey on 02.04.2017.
 */
@Entity
@Table(name = "authorities")
public class ZUserAuthority implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="username", nullable=false, updatable=false)
    @JsonIgnore
    private ZUser user;

    private String authority;

    public ZUserAuthority(ZUser user, String authority) {
        this.user = user;
        this.authority = authority;
    }

    public ZUserAuthority(String authority) {
        this.authority = authority;
    }

    public ZUserAuthority() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZUser getUser() {
        return user;
    }

    public void setUser(ZUser user) {
        this.user = user;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
