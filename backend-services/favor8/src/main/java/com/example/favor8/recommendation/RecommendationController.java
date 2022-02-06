package com.example.favor8.recommendation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RECOMMENDATION", description = "personalized recommendation")
@RequestMapping("recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("{userId}")
    public String recommend(@PathVariable Integer userId) {
        log.info("user {} requests a recommendation", userId);
        return recommendationService.recommend(userId);
    }
}
