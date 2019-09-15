package com.patex.zombie.gateway.service;

import com.patex.jwt.JwtTokenUtil;
import com.patex.model.Creds;
import com.patex.zombie.gateway.api.UserApi;
import com.patex.zombie.gateway.model.JwtAuthentication;
import feign.FeignException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final JwtTokenUtil tokenUtil;

    private final UserApi userApi;

    public UserService(JwtTokenUtil tokenUtil, @Lazy UserApi userApi) {
        this.tokenUtil = tokenUtil;
        this.userApi = userApi;
    }

    public Mono<Authentication> authenticate(String username, String password) {
        return Mono.fromSupplier(() -> {

            String token = tokenUtil.generateToken("gateway", "ROLE_GET_TOKEN");
            try {
                String userToken = userApi.authenticate("Bearer " + token, new Creds(username, password));
                return new JwtAuthentication(userToken, username);
            } catch (FeignException e) {
                if (e.status() == 401) {
                    throw new UnauthorizedException(e);
                } else {
                    throw e;
                }
            }
        });
    }
}
