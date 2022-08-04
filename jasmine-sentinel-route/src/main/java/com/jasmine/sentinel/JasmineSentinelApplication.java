package com.jasmine.sentinel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class JasmineSentinelApplication {
    public static void main(String[] args) {
        SpringApplication.run(JasmineSentinelApplication.class, args);
        log.info("## jasmine-sentinel-application run successfully. ##");
    }
}
