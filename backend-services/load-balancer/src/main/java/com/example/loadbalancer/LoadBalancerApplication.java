package com.example.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class LoadBalancerApplication {

    private final ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(LoadBalancerApplication.class, args);
    }

    @GetMapping
    public String home() {
        System.out.println(applicationContext.getApplicationName());
        System.out.println(applicationContext.getDisplayName());
        return "Welcome to Favor8 Movie Recommendation Service!\n";
    }
}
