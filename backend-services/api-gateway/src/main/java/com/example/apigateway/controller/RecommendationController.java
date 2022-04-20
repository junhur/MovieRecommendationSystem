package com.example.apigateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("recommend")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RestTemplate restTemplate;
    private final static String requestUrl = "http://model_inference:8000/recommend/";

    @GetMapping("{userId}")
    public String recommend(@PathVariable Integer userId) {

        try {
            ResponseEntity<String> res = restTemplate.getForEntity(requestUrl + userId, String.class);
            log.info("called model api, response: {}", res.getBody());
            String response = res.getBody().substring(1, res.getBody().length() -1);
            String[] recommendations = response.split(",");
            StringBuilder responseBuilder = new StringBuilder();
            List<String> recommendationResults = new ArrayList<>();
            for (int i= 0; i < recommendations.length-1; i ++) {
                responseBuilder.append(recommendations[i]);
                if (i != recommendations.length -2) {
                    responseBuilder.append(", ");
                }
                recommendationResults.add(recommendations[i]);
            }
            String recommendationResponse = responseBuilder.toString();
            recommendationResponse = recommendationResponse.substring(1, recommendationResponse.length()-1);
            return recommendationResponse;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.warn("Can't connect to ML services, returning default recommendation.");
            return "big+hero+6+2014,avatar+2009,gone+girl+2014,the+hunger+games+mockingjay+-+part+1+2014,pulp+fiction+1994,the+dark+knight+2008,blade+runner+1982,the+avengers+2012,the+maze+runner+2014,dawn+of+the+planet+of+the+apes+2014,whiplash+2014,fight+club+1999,guardians+of+the+galaxy+2014,the+shawshank+redemption+1994,forrest+gump+1994,pirates+of+the+caribbean+the+curse+of+the+black+pearl+2003,star+wars+1977,schindlers+list+1993,rise+of+the+planet+of+the+apes+2011,the+godfather+1972";
        }
    }
}
