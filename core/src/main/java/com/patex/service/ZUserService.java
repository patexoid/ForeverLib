package com.patex.service;

import com.patex.LibException;
import com.patex.entities.ZUser;
import com.patex.entities.ZUserAuthority;
import com.patex.entities.ZUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by Alexey on 25.03.2017.
 */
@Service
public class ZUserService implements UserDetailsService {

    public static final String GUEST = "GUEST";
    public static final String USER = "ROLE_USER";
    public static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    private final ZUser anonim = new ZUser("anonimus", true);

    @Autowired
    private ZUserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ZUser loadUserByUsername(String username) throws UsernameNotFoundException {
        ZUser user = userRepo.findOne(username);
        return user == null ? anonim : user;
    }

    public ZUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof ZUser) {
            return (ZUser) principal;
        }
        return anonim;
    }

    @Secured(USER)
    public ZUser save(ZUser user) {
        ZUser currentUser = getCurrentUser();
        if (currentUser.getUsername().equals(user.getUsername()) ||
                currentUser.getAuthorities().stream().anyMatch(o -> ADMIN_AUTHORITY.equals(o.getAuthority()))) {
            ZUser updatedUser = new ZUser();
            BeanUtils.copyProperties(user, updatedUser);
            updatedUser.setPassword(currentUser.getPassword());
            return userRepo.save(updatedUser);
        }
        return user;
    }

    public ZUser createUser(ZUser user) {
        user.getAuthorities().add(new ZUserAuthority(user, USER));
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Secured(USER)
    public void updatePassword(String oldPassword, String newPassword) throws LibException {
        ZUser currentUser = getCurrentUser();
        if (currentUser == anonim) {
            throw new LibException("Unable to change password for anonim user");
        }
        ZUser user = loadUserByUsername(currentUser.getUsername());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new LibException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}