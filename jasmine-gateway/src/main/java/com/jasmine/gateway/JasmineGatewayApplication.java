package com.jasmine.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class JasmineGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(JasmineGatewayApplication.class, args);
        log.info("## jasmine-gateway-application run successfully. ##");
    }
}
