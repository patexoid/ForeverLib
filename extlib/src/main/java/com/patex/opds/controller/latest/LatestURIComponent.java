package com.patex.opds.controller.latest;

import com.patex.jwt.JwtTokenUtil;
import com.patex.opds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexey on 09.04.2017.
 */
@Component
public class LatestURIComponent {

    @Autowired
    private UserService userService;

    private final Map<String, ModelAndView> latestForUser = new HashMap<>();

    public void afterMethod(ModelAndView view) {
        latestForUser.put(userService.getCurrentUser(), view);
    }

    @Secured(JwtTokenUtil.USER)
    public ModelAndView getLatestForCurrentUser(){
        return latestForUser.get(userService.getCurrentUser());
    }
}
