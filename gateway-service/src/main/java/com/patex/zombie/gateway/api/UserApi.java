package com.patex.zombie.gateway.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.patex.model.Creds;

@FeignClient(name = "user-service")
public interface UserApi {

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    String authenticate(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorization, Creds creds);
}
