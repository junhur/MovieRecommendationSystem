package com.example.favor8.evaluation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "EVALUATION", description = "online evaluation")
@RequestMapping("evaluate")
public class OnlineEvaluationController {

    private final OnlineEvaluationService evaluationService;

    @GetMapping
    public String evaluate() {
        return evaluationService.evaluate();
    }
}
