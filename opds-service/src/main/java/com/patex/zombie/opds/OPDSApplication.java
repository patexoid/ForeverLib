package com.patex.zombie.opds;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.patex.zombie.opds", "com.patex.jwt","com.patex.security","com.patex.utils"})
@EnableDiscoveryClient
@RequiredArgsConstructor
@EnableFeignClients
@EnableScheduling
public class OPDSApplication {
    public static void main(String[] args) {
        SpringApplication.run(OPDSApplication.class, args);
    }
}
