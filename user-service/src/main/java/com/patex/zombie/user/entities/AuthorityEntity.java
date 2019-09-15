package com.patex.zombie.user.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 * Created by Alexey on 02.04.2017.
 */
@Entity
@Table(name = "authorities")
public class AuthorityEntity implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="username", nullable=false, updatable=false)
    @JsonIgnore
    private UserEntity user;

    private String authority;

    public AuthorityEntity(UserEntity user, String authority) {
        this.user = user;
        this.authority = authority;
    }

    public AuthorityEntity(String authority) {
        this.authority = authority;
    }

    public AuthorityEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
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
