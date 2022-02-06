package com.example.favor8.testing;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("playground")
@RequiredArgsConstructor
@Hidden
@Slf4j
public class PlaygroundController {

    private final RestTemplate restTemplate;

    @GetMapping("rest")
    public void test() {
        try {
            ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:8000/", String.class);
            log.info("response from port 8000: {}", res.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
