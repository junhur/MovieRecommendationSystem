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

@RestController
@RequestMapping("recommend")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RestTemplate restTemplate;

    @GetMapping("{userId}")
    public String recommend(@PathVariable Integer userId) {

        String req = String.format("[{\"user_id\": \"%s\",\"movie_title\": \"default\",\"score\": -1}]", userId);

        try {
            ResponseEntity<ResponseDto> res = restTemplate.postForEntity(
                    "http://20.65.16.87/api/v1/service/svd-model-r/score/",
                    req,
                    ResponseDto.class
            );
            ResponseDto responseDto = res.getBody();
            log.info("req from user {}, res: {}", userId, res.getBody());
            if (res.getStatusCode() != HttpStatus.OK || responseDto == null) {
                throw new Exception("request not successful or empty response");
            }
            return String.join(",", responseDto.getRecommendations());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.warn("Can't connect to ML services, returning default recommendation.");
            return "big+hero+6+2014,avatar+2009,gone+girl+2014,the+hunger+games+mockingjay+-+part+1+2014,pulp+fiction+1994,the+dark+knight+2008,blade+runner+1982,the+avengers+2012,the+maze+runner+2014,dawn+of+the+planet+of+the+apes+2014,whiplash+2014,fight+club+1999,guardians+of+the+galaxy+2014,the+shawshank+redemption+1994,forrest+gump+1994,pirates+of+the+caribbean+the+curse+of+the+black+pearl+2003,star+wars+1977,schindlers+list+1993,rise+of+the+planet+of+the+apes+2011,the+godfather+1972";
        }
    }
}
