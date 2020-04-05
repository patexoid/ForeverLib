package com.patex.zombie.service;

import com.patex.zombie.model.User;

import java.util.Collection;
import java.util.Locale;

public interface UserService {


    String GUEST = "GUEST";
    String USER = "ROLE_USER";
    String ADMIN_AUTHORITY = "ROLE_ADMIN";

    User getCurrentUser();

    Collection<User> getByRole(String role);

    Locale getUserLocale();

    User getUser(String user);
}
