package com.patex.service;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserAuthority;

public class UserCreationEvent {

    private final ZUser user;

    public UserCreationEvent(ZUser user) {
        this.user = user;
    }

    public ZUser getUser() {
        return user;
    }

    public boolean isAdmin() {
        return user.getAuthorities().stream().
                map(ZUserAuthority::getAuthority).
                anyMatch(ZUserService.ADMIN_AUTHORITY::equals);
    }

}
