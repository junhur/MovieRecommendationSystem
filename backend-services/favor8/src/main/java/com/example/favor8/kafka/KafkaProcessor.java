package com.example.favor8.kafka;

import com.example.favor8.dao.entity.*;
import com.example.favor8.dao.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProcessor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final WatchingRepository watchingRepository;
    private final RecommendationRequestRepository recommendationRequestRepository;

    private final RestTemplate restTemplate;

    public void process_recommendation_request(String message) throws Exception {
        if (message.contains("recommendation request")) {
            try {
                RecommendationRequestPo recPo = new RecommendationRequestPo();
                String[] messageByKeyword = message.split("recommendation request ");
                // Read time and user
                String[] timeAndUser = messageByKeyword[0].split(",");
                recPo.setRequestedAt(LocalDateTime.parse(timeAndUser[0]));
                String userId = timeAndUser[1];
                recPo.setUserId(Integer.parseInt(userId));
                storeUser(Integer.parseInt(userId));

                // Read status
                String[] messageByResult = messageByKeyword[1].split("result: ");
                String[] serverAndStatus = messageByResult[0].split(",");
                String status = serverAndStatus[1];
                recPo.setStatus(Integer.parseInt(status.substring(status.length()-3)));

                // Read recommendations and response time
                int lastCommaIdx = messageByResult[1].lastIndexOf(",");
                String result = messageByResult[1].substring(0, lastCommaIdx-1);
                String[] resultArray = result.split(",");
                recPo.setResults(Arrays.asList(resultArray));
                String response_time = messageByResult[1].substring(lastCommaIdx+2);
                recPo.setResponseTime(Integer.parseInt(response_time.substring(0, response_time.length()-3)));
                recommendationRequestRepository.saveAndFlush(recPo);
            } catch (Exception e) {
                // Handling exceptions for potential string manipulation exceptions
                log.error(message, e.getMessage());
            }
        } else {
            throw new Exception("Unsupported log type");
        }
    }

    /**
     *
     * @param message: Kafka message
     *               <time>,<userid>,GET /data/m/<movieid>/<minute>.mpg
     *               <time>,<userid>,GET /rate/<movieid>=<rating>
     * @throws Exception logged
     */
    public void process(String message) throws Exception {
        String[] split = message.split(",GET /");
        String timeUser = split[0];

        String time = timeUser.split(",")[0];
        String userId = timeUser.split(",")[1];

        String request =  split[1];

        // data quality check
        if (isMalformed(time, userId, request)) {
            return;
        }

        storeUser(Integer.parseInt(userId));

        if (request.startsWith("data")) {
            // request format: data/m/<movieid>/<minute>.mpg
            String movieMinute = request.substring(7);
            String movie = movieMinute.split("/")[0];
            String minute = movieMinute.split("/")[1].split("\\.")[0];
            storeMovie(movie);
            storeWatching(time, userId, movie, minute);
        } else {
            // request format: rate/<movieid>=<rating>
            String movieRating = request.substring(5);
            storeMovie(request.substring(5).split("=")[0]);
            storeRating(
                    time,
                    userId,
                    movieRating.split("=")[0], // movie
                    movieRating.split("=")[1]  // rating
            );
        }
    }

    /**
     *
     * @param time time must be not empty
     * @param userId userId must be an integer
     * @param request request message must not be empty
     * @return true if the data is malformed
     */
    private boolean isMalformed(String time, String userId, String request) {
        if (time.isEmpty() || userId.isEmpty() || request.isEmpty()) {
            log.warn("missing data, time: {}, userId {}, request: {}", time, userId, request);
            return true;
        } else if (!StringUtils.isNumeric(userId)) {
            log.warn("userId must be an integer: {}", userId);
            return true;
        } else if (!request.startsWith("data") && !request.startsWith("rate")) {
            log.warn("unknown request: {}", request);
            return true;
        }
        return false;
    }

    /**
     * Store user info if unseen
     * @param id: userId
     * @throws Exception logged
     */
    private void storeUser(Integer id) throws Exception {
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
                throw new Exception("user "+ id + " not existed");
            }
            userRepository.saveAndFlush(user);
        } catch (Exception e) {
            throw new Exception (id + ": " + e.getMessage());
        }
    }

    /**
     * Store movie info if unseen
     * @param title: movie tile as id
     * @throws Exception logged
     */
    private void storeMovie(String title) throws Exception {
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
            throw new Exception("movie " + title + " does not exist: " + e.getMessage());
        }
    }

    private void storeRating(String time, String userId, String movie, String rating) {
        RatingPo ratingPo = new RatingPo();
        ratingPo.setUserId(Integer.parseInt(userId));
        ratingPo.setMovieTitle(movie);
        ratingPo.setScore(Integer.parseInt(rating));
        ratingPo.setRatedAt(LocalDateTime.parse(time));
        ratingRepository.saveAndFlush(ratingPo);
    }

    private void storeWatching(String time, String userId, String movie, String minute) {
        WatchingPo watching = new WatchingPo();
        watching.setUserId(Integer.parseInt(userId));
        watching.setMovieTitle(movie);
        watching.setMinute(Integer.parseInt(minute));
        watching.setWatchedAt(LocalDateTime.parse(time));
        watchingRepository.saveAndFlush(watching);
    }
}
