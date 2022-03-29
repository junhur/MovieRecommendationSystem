package com.example.favor8.evaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnlineEvaluationService {

    private final RestTemplate restTemplate;

    public String evaluate() {
        try {
            ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:8000/evaluate", String.class);
            return res.getBody();
        } catch (Exception e) {
            log.warn("Something has gone wrong: {}", e.getMessage());
            return "";
        }
    }
}
