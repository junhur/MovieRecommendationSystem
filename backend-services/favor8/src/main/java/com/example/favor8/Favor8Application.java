package com.example.favor8;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Favor8Application {

    public static void main(String[] args) {
        SpringApplication.run(Favor8Application.class, args);
    }

    @GetMapping
    @Operation(hidden = true)
    public String home() {
        return "Welcome to Favor8 Movie Recommendation Service!\n";
    }
}
