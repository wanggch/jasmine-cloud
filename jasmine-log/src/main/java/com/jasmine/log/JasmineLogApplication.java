package com.jasmine.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class JasmineLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(JasmineLogApplication.class, args);
        log.info("## jasmine-log-application run successfully. ##");
    }
}
