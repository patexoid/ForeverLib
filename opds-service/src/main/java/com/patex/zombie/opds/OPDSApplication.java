package com.patex.zombie.opds;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.patex.zombie.opds", "com.patex.jwt","com.patex.security"})
@EnableDiscoveryClient
@RequiredArgsConstructor
public class OPDSApplication {
}
