package com.patex.zombie.opds.controller;

import com.patex.zombie.opds.service.RootProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.function.Supplier;

@Controller
@RequestMapping("root")
@RequiredArgsConstructor
public class RootController {

    private final RootProvider provider;


    private final DiscoveryClient discoveryClient;

    @RequestMapping("register")
    @Secured("OPDS_ROOT_REGISTRATION")
    public void register(String service, String rootId){
        Supplier<Boolean> exists=()->discoveryClient.getServices().stream().anyMatch(service::equals);
        provider.registerRoot(rootId, exists);
    }
}
