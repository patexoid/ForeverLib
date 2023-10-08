package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class ZUser implements UserDetails, CredentialsContainer {

    @Id
    private String username;

    private boolean enabled;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "user")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ZUserAuthority> authorities = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "user")
    @JsonIgnore
    private ZUserConfigEntity userConfig;


    public ZUser() {
    }

    public ZUser(String username, boolean enabled) {
        this.username = username;
        this.enabled = enabled;
    }

    @Override
    public Collection<ZUserAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    public ZUserConfigEntity getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(ZUserConfigEntity userConfig) {
        this.userConfig = userConfig;
    }

}
