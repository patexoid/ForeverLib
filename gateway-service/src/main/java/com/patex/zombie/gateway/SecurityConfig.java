package com.patex.zombie.gateway;

import com.patex.zombie.gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .anyExchange().permitAll()
                .and().authenticationManager(reactiveAuthenticationManager()).csrf().disable()
                .httpBasic().and()
                .build();
    }

    @Bean
    ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication ->
                userService.authenticate(authentication.getName(), (String) authentication.getCredentials());
    }
}