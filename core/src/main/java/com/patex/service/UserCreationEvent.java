package com.patex.service;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserAuthority;
import com.patex.zombie.service.UserService;

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
                anyMatch(UserService.ADMIN_AUTHORITY::equals);
    }

}
