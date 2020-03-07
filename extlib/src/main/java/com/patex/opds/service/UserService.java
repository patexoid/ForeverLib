package com.patex.opds.service;

import com.patex.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserService {


    public String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }
        return null;
    }

    public Locale getUserLocale(String userId) {
        return Locale.getDefault();
    }
}
