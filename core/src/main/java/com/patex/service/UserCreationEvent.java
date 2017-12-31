package com.patex.service;

import com.patex.entities.ZUser;

public class UserCreationEvent {

    private final ZUser user;

    public UserCreationEvent(ZUser user) {
        this.user = user;
    }

    public ZUser getUser() {
        return user;
    }

    public boolean isAdmin() {
        return user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(ZUserService.ADMIN_AUTHORITY));
    }

}
