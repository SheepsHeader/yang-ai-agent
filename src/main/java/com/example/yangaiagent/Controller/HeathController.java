package com.example.yangaiagent.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathController {
    @GetMapping("/health")
    public String health() {
        return "healthy";
    }
}
