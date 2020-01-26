package com.patex.zombie.user.controller;

import com.patex.jwt.JwtTokenUtil;
import com.patex.model.Creds;
import com.patex.zombie.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("/authenticate")
@CrossOrigin
@RequiredArgsConstructor
public class JwtAuthenticationController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userDetailsService;

    @RequestMapping(method = RequestMethod.POST)
    @Secured("ROLE_GET_TOKEN")
    public @ResponseBody
    String createAuthenticationToken(@RequestBody Creds creds) {
        return userDetailsService.findUser(creds.getUsername(), creds.getPassword()).
                map(jwtTokenUtil::generateToken).orElse(null);
    }
}
