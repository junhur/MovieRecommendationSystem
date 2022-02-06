package com.example.favor8.kafka;

import com.example.favor8.dao.entity.MoviePo;
import com.example.favor8.dao.entity.RatingPo;
import com.example.favor8.dao.entity.UserPo;
import com.example.favor8.dao.entity.WatchingPo;
import com.example.favor8.dao.repository.MovieRepository;
import com.example.favor8.dao.repository.RatingRepository;
import com.example.favor8.dao.repository.UserRepository;
import com.example.favor8.dao.repository.WatchingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProcessor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final WatchingRepository watchingRepository;

    private final RestTemplate restTemplate;

    // todo: refactor the string split
    public void process(String message) {
        String[] split = message.split(",GET /");

        String userId = split[0].split(",")[1];
        storeUser(Integer.parseInt(userId));

        if (split[1].startsWith("rate")) {
            storeMovie(split[1].substring(5).split("=")[0]);
            storeRating(split[0], split[1].substring(5));
        } else {
            storeMovie(split[1].substring(7).split("/")[0]);
            storeWatching(split[0], split[1].substring(7));
        }
    }

    private void storeUser(Integer id) {

        if (userRepository.existsById(id)) {
            return;
        }

        try {
            ResponseEntity<String> res = restTemplate.getForEntity("http://128.2.204.215:8080/user/" + id, String.class);
            JsonNode node = objectMapper.readTree(res.getBody());
            UserPo user = new UserPo();
            user.setId(id);
            user.setAge(node.get("age").asInt());
            user.setOccupation(node.get("occupation").asText());
            user.setGender(node.get("gender").asText());
            if (user.getId() == null) {
                log.warn("user {} not existed", id);
            }
            userRepository.saveAndFlush(user);
        } catch (Exception e) {
            log.error(id.toString(), e.getMessage());
        }
    }

    private void storeMovie(String title) {

        if (movieRepository.existsById(title)) {
            return;
        }

        try {
            ResponseEntity<String> res = restTemplate.getForEntity("http://128.2.204.215:8080/movie/" + title, String.class);

            JsonNode node = objectMapper.readTree(res.getBody());
            MoviePo movie = new MoviePo();
            movie.setTitle(title);
            movie.setInfo(node.toString());
            movieRepository.saveAndFlush(movie);
        } catch (Exception e) {
            log.warn("movie {} not existed", title);
        }
    }

    private void storeRating(String str1, String str2) {
        RatingPo rating = new RatingPo();
        rating.setUserId(Integer.parseInt(str1.split(",")[1]));
        rating.setMovieTitle(str2.split("=")[0]);
        rating.setScore(Integer.parseInt(str2.split("=")[1]));
        rating.setRatedAt(LocalDateTime.parse(str1.split(",")[0]));
        ratingRepository.saveAndFlush(rating);
    }

    private void storeWatching(String str1, String str2) {
        WatchingPo watching = new WatchingPo();
        watching.setUserId(Integer.parseInt(str1.split(",")[1]));
        watching.setMovieTitle(str2.split("/")[0]);
        watching.setMinute(Integer.parseInt(str2.split("/")[1].split("\\.")[0]));
        watching.setWatchedAt(LocalDateTime.parse(str1.split(",")[0]));
        watchingRepository.saveAndFlush(watching);
    }
}
