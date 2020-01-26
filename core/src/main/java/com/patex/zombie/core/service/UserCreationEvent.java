package com.patex.zombie.core.service;


import com.patex.model.User;

public class UserCreationEvent {

    private final User user;

    public UserCreationEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isAdmin() {
        return false;
    }

}
