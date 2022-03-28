package com.example.loadbalancer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("recommend")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RestTemplate restTemplate;

    @GetMapping("{userId}")
    public String recommend(@PathVariable Integer userId) {
        return "";
    }

}
