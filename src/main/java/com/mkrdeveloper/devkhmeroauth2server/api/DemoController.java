package com.mkrdeveloper.devkhmeroauth2server.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController
{

    @GetMapping("/private")
    public Map<String, String> privateApi() {
        return Map.of("message", "Hello Private World!");
    }

    @GetMapping("/public")
    public Map<String, String> publicApi() {
        return Map.of("message", "Hello World!");
    }

}
