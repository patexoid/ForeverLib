package com.patex.opds.controller.latest;

import com.patex.zombie.core.entities.ZUser;
import com.patex.zombie.core.service.ZUserService;
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
    private ZUserService userService;

    private final Map<String, ModelAndView> latestForUser = new HashMap<>();

    public void afterMethod(ModelAndView view) {
        ZUser currentUser = userService.getCurrentUser();
        latestForUser.put(currentUser.getUsername(), view);
    }

    @Secured(ZUserService.USER)
    public ModelAndView getLatestForCurrentUser(){
        return latestForUser.get(userService.getCurrentUser().getUsername());
    }
}
