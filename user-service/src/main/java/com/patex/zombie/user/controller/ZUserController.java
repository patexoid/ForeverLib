package com.patex.zombie.user.controller;

import com.patex.LibException;
import com.patex.model.User;
import com.patex.zombie.user.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class ZUserController {

    private final UserService userService;

    @RequestMapping(method = RequestMethod.POST)
    @Secured(UserService.USER)
    public User save(User user, Authentication authentication) {
        return userService.save(user, (User) authentication.getPrincipal());
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody
    User createUser(@RequestBody UserCreateRequest user, Authentication authentication) {
        return userService.createUser(user, (User) authentication.getPrincipal());
    }

    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @Secured(UserService.USER)
    @ResponseStatus(value = HttpStatus.OK)
    public void updatePassword(@RequestBody PasswordChangeRequest request,
                               Authentication authentication) throws LibException {
        userService.updatePassword(request.getOld(), request.getNewPassword(), (User) authentication.getPrincipal());
    }

    @Data
    public static class PasswordChangeRequest {
        private String old;
        private String newPassword;
    }
}
