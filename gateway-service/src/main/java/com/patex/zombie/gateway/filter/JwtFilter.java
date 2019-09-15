package com.patex.zombie.gateway.filter;

import com.patex.zombie.gateway.model.JwtAuthentication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    public JwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return exchange.getPrincipal()
                    .cast(JwtAuthentication.class)
                    .map(JwtAuthentication::getToken).
                            map(token -> exchange.getRequest().mutate()
                                    .headers(headers -> {
                                        headers.remove(HttpHeaders.AUTHORIZATION);
                                        headers.add(HttpHeaders.AUTHORIZATION,
                                                "Bearer " + token);
                                    })
                                    .build()
                            )
                    .map(request -> exchange.mutate().request(request).build())
                    .flatMap(chain::filter);
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}
