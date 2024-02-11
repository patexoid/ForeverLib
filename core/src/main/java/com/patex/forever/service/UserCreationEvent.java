package com.patex.forever.service;

import com.patex.forever.entities.LibUser;
import com.patex.forever.entities.LibUserAuthority;

public class UserCreationEvent {

    private final LibUser user;

    public UserCreationEvent(LibUser user) {
        this.user = user;
    }

    public LibUser getUser() {
        return user;
    }

    public boolean isAdmin() {
        return user.getAuthorities().stream().
                map(LibUserAuthority::getAuthority).
                anyMatch(UserService.ADMIN_AUTHORITY::equals);
    }

}
