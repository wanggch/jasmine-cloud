package com.jasmine.log.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class HelloController {

    @GetMapping("/hello")
    public String index() {
        return "hello world.";
    }
}
