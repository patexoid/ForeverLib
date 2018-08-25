package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class ZUser implements UserDetails, CredentialsContainer {

    @Id
    private String username;

    @JsonIgnore
    private boolean enabled;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "user")
    @Fetch(value = FetchMode.SUBSELECT)
    @JsonIgnore
    private List<ZUserAuthority> authorities = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "user")
    @JsonIgnore
    private ZUserConfig userConfig;


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

    public ZUserConfig getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(ZUserConfig userConfig) {
        this.userConfig = userConfig;
    }

    @JsonGetter
    public Set<String> getPermissions(){
        return authorities.stream().map(ZUserAuthority::getAuthority).collect(Collectors.toSet());
    }

}
