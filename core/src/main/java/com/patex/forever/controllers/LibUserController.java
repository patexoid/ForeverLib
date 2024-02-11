package com.patex.forever.controllers;

import com.patex.forever.entities.LibUser;
import com.patex.forever.LibException;
import com.patex.forever.entities.LibUserConfigEntity;
import com.patex.forever.service.LibUserService;
import com.patex.forever.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.patex.forever.service.UserService.USER;

/**
 * Created by Alexey on 25.03.2017.
 */
@Controller
@RequestMapping("/user")
public class LibUserController {

    @Autowired
    private LibUserService userDetailsService;


    @RequestMapping(method = RequestMethod.POST)
    @Secured(USER)
    public User save(User user) {
        return userDetailsService.save(user);
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody
    LibUser createUser(@RequestBody LibUser user) {
        return userDetailsService.createUser(user);
    }


    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @Secured(USER)
    public void updatePassword(@RequestParam("old") String oldPassword, @RequestParam("new") String newPassword)
            throws LibException {
        userDetailsService.updatePassword(oldPassword, newPassword);
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public @ResponseBody
    User getCurrentUser() {
        return userDetailsService.getCurrentUser();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
    }

    @RequestMapping(value = "/updateConfig", method = RequestMethod.GET)
    @Secured(USER)
    public void updateUserConfig(LibUserConfigEntity newConfig) {
        userDetailsService.updateUserConfig(newConfig);
    }
}
