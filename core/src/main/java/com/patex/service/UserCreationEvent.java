package com.patex.service;

import com.patex.entities.UserEntity;
import com.patex.entities.AuthorityEntity;

public class UserCreationEvent {

    private final UserEntity user;

    public UserCreationEvent(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public boolean isAdmin() {
        return user.getAuthorities().stream().
                map(AuthorityEntity::getAuthority).
                anyMatch(ZUserService.ADMIN_AUTHORITY::equals);
    }

}
