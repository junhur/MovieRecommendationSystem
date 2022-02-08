package com.example.favor8.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final RestTemplate restTemplate;

    public String recommend(Integer userId) {
        try {
            ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:8000/recommend/" + userId, String.class);
            log.info("called model api, response: {}", res.getBody());
            return res.getBody().substring(1, res.getBody().length() - 1);
        } catch (Exception e) {
            log.warn("Can't connect to ML services, returning default recommendation.");
            return "big+hero+6+2014,avatar+2009,gone+girl+2014,the+hunger+games+mockingjay+-+part+1+2014,pulp+fiction+1994,the+dark+knight+2008,blade+runner+1982,the+avengers+2012,the+maze+runner+2014,dawn+of+the+planet+of+the+apes+2014,whiplash+2014,fight+club+1999,guardians+of+the+galaxy+2014,the+shawshank+redemption+1994,forrest+gump+1994,pirates+of+the+caribbean+the+curse+of+the+black+pearl+2003,star+wars+1977,schindlers+list+1993,rise+of+the+planet+of+the+apes+2011,the+godfather+1972";
        }
    }
}
