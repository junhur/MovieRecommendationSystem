package com.example.favor8.kafka;

import com.example.favor8.dao.entity.*;
import com.example.favor8.dao.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaProcessorTest {
    // Mock User/Movie information response body
    private final String userInfoBody = "{\"user_id\":514537,\"age\":31,\"occupation\":\"college/grad student\",\"gender\":\"M\"}";
    private final String userNotFoundBody = "{\"message\":\"user not found\"}";
    private final String movieInfoBody = "{\"id\":\"despicable+me+2+2013\",\"tmdb_id\":93456,\"imdb_id\":\"tt1690953\",\"title\":\"Despicable Me 2\",\"original_title\":\"Despicable Me 2\",\"adult\":\"False\",\"belongs_to_collection\":{\"id\":86066,\"name\":\"Despicable Me Collection\",\"poster_path\":\"/xIXhIlZDRmSSfNbpN7kBCm5hg39.jpg\",\"backdrop_path\":\"/15IZl405E664QDVxpFJBl7TtLmw.jpg\"},\"budget\":\"76000000\",\"genres\":[{\"id\":16,\"name\":\"Animation\"},{\"id\":35,\"name\":\"Comedy\"},{\"id\":10751,\"name\":\"Family\"}],\"homepage\":\"http://www.despicableme.com/\",\"original_language\":\"en\",\"overview\":\"Gru is recruited by the Anti-Villain League to help deal with a powerful new super criminal.\",\"popularity\":\"24.82355\",\"poster_path\":\"/kQrYyZQHkwkUg2KlUDyvymj9FAp.jpg\",\"production_companies\":[{\"name\":\"Universal Pictures\",\"id\":33},{\"name\":\"Illumination Entertainment\",\"id\":6704}],\"production_countries\":[{\"iso_3166_1\":\"US\",\"name\":\"United States of America\"}],\"release_date\":\"2013-06-25\",\"revenue\":\"970761885\",\"runtime\":98,\"spoken_languages\":[{\"iso_639_1\":\"en\",\"name\":\"English\"}],\"status\":\"Released\",\"vote_average\":\"7.0\",\"vote_count\":\"4729\"}";
    private final String movieNotFoundBody = "{\"message\":\"movie not found\"}";

    // Mock Repositories to be used in test
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final MovieRepository movieRepository = Mockito.mock(MovieRepository.class);
    private final RatingRepository ratingRepository = Mockito.mock(RatingRepository.class);
    private final WatchingRepository watchingRepository = Mockito.mock(WatchingRepository.class);
    private final RecommendationRequestRepository recommendationRequestRepository = Mockito.mock(RecommendationRequestRepository.class);

    // Mock RestTemplate
    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    //ArgumentCaptors
    private final ArgumentCaptor<RecommendationRequestPo> recommendationRequestPoArgumentCaptor = ArgumentCaptor.forClass(RecommendationRequestPo.class);
    private final ArgumentCaptor<UserPo> userPoArgumentCaptor = ArgumentCaptor.forClass(UserPo.class);
    private final ArgumentCaptor<MoviePo> moviePoArgumentCaptor = ArgumentCaptor.forClass(MoviePo.class);
    private final ArgumentCaptor<WatchingPo> watchingPoArgumentCaptor = ArgumentCaptor.forClass(WatchingPo.class);
    private final ArgumentCaptor<RatingPo> ratingPoArgumentCaptor = ArgumentCaptor.forClass(RatingPo.class);

    // Parameter Kafka message Strings for Recommendation Request processing
    private final String recRequest = "2022-03-04T20:22:00.415793,514537,recommendation request 17645-team08.isri.cmu.edu:8082, status 200, result: fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997, 391 ms";
    private final String incorrectTypeRecRequest = "REQUESTED_TIME,USER_ID,recommendation request 17645-team08.isri.cmu.edu:8082, status twohundred, result: fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997, TEN ms";
    private final String whiteSpaceRecRequest = "2022-03-04T20:22:00.415793,     514537,    recommendation request 17645-team08.isri.cmu.edu:8082, status      200, result:     fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997, 391       ms";

    // Rating and Watching messages
    private final String rateMovie = "2022-03-04T20:16:46,514537,GET /rate/despicable+me+2+2013=4";
    private final String watchMovie = "2022-03-16T00:29:05,514537,GET /data/m/despicable+me+2+2013/90.mpg";

    // Tests for parse_recommendation_request
    @Test
    @DisplayName("Parse valid recommendation request")
    void parseRecommendationRequest() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        try {
            String[] result = processor.parseRecommendationRequest(recRequest);
            String[] expected = {
                    "2022-03-04T20:22:00.415793",
                    "514537",
                    "200",
                    "fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997",
                    "391"
            };
            assertArrayEquals(expected, result);
        } catch (Exception e) {
            // Exception will not occur: do nothing
        }
    }

    @Test
    @DisplayName("Still parses whitespace recommendation request correctly")
    void parseWhiteSpacedRecommendationRequest() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        try {
            String[] result = processor.parseRecommendationRequest(whiteSpaceRecRequest);
            String[] expected = {
                    "2022-03-04T20:22:00.415793",
                    "514537",
                    "200",
                    "fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997",
                    "391"
            };
            assertArrayEquals(expected, result);
        } catch (Exception e) {
            // Exception will not occur: do nothing
        }
    }

    @Test
    @DisplayName("Still parses incorrectly typed recommendation request correctly")
    void parseIncorrectTypesRecommendationRequest() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        try {
            String[] result = processor.parseRecommendationRequest(incorrectTypeRecRequest);
            String[] expected = {
                    "REQUESTED_TIME",
                    "USER_ID",
                    "twohundred",
                    "fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997",
                    "TEN"
            };
            assertArrayEquals(expected, result);
        } catch (Exception e) {
            // Exception will not occur: do nothing
        }
    }

    @Test
    @DisplayName("Watch Log message is passed in to parse as a recommendation request")
    void parseRecommendationRequestInvalidMessage() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.parseRecommendationRequest(watchMovie);
        });
        assertTrue(exception.getMessage().contains("Recommendation request not in expected format"));
    }

    // Tests for process_recommendation_request
    @Test
    @DisplayName("Happy path process recommendation request")
    void processRecommendationRequest() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        try {
            ResponseEntity<String> userInfo = ResponseEntity.ok(userInfoBody);
            Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/user/514537", String.class)).thenReturn(userInfo);

            processor.processRecommendationRequest(recRequest);

            Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(ArgumentMatchers.any(UserPo.class));
            Mockito.verify(recommendationRequestRepository, Mockito.times(1)).saveAndFlush(recommendationRequestPoArgumentCaptor.capture());

            RecommendationRequestPo recPo = recommendationRequestPoArgumentCaptor.getValue();
            assertEquals(recPo.getUserId(), 514537);
            assertEquals(recPo.getRequestedAt(), LocalDateTime.parse("2022-03-04T20:22:00.415793"));
            assertEquals(recPo.getResponseTime(), 391);
            assertEquals(recPo.getStatus(), 200);
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    @DisplayName("Throws an exception when message type is not recommendation request")
    void processRecommendationRequestUnsupportedMessageType() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.processRecommendationRequest(watchMovie);
        });
        assertTrue(exception.getMessage().contains("Unsupported log type"));
    }

    @Test
    @DisplayName("Message to be parsed has all strings: cannot be parsed to Integer or LocalDateTime")
    void processRecommendationRequestIncorrectTypes() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.processRecommendationRequest(incorrectTypeRecRequest);
        });
        assertTrue(exception.getMessage().contains("Error occurred while parsing and storing rec request"));
    }

    @Test
    @DisplayName("Process and parse correctly formatted watch log")
    void processWatchLog() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        try {
            ResponseEntity<String> userInfo = ResponseEntity.ok(userInfoBody);
            Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/user/514537", String.class)).thenReturn(userInfo);

            ResponseEntity<String> movieInfo = ResponseEntity.ok(movieInfoBody);
            Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/movie/despicable+me+2+2013", String.class)).thenReturn(movieInfo);

            processor.process(watchMovie);

            Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(userPoArgumentCaptor.capture());
            Mockito.verify(movieRepository, Mockito.times(1)).saveAndFlush(moviePoArgumentCaptor.capture());
            Mockito.verify(watchingRepository, Mockito.times(1)).saveAndFlush(watchingPoArgumentCaptor.capture());

            UserPo userPo = userPoArgumentCaptor.getValue();
            assertEquals(userPo.getId(), 514537);
            assertEquals(userPo.getAge(), 31);
            assertEquals(userPo.getGender(), "M");
            MoviePo moviePo = moviePoArgumentCaptor.getValue();
            assertEquals(moviePo.getTitle(), "despicable+me+2+2013");
            WatchingPo watchingPo = watchingPoArgumentCaptor.getValue();
            assertEquals(watchingPo.getMinute(), 90);
            assertEquals(watchingPo.getWatchedAt(), LocalDateTime.parse("2022-03-16T00:29:05"));
        } catch (Exception e) {
            // do nothing. No exceptions should throw
        }
    }

    @Test
    @DisplayName("Process and parse correctly formatted watch log without creating new user and movie")
    void processWatchLogUserAndMovieExists() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        try {
            Mockito.when(userRepository.existsById(514537)).thenReturn(true);
            Mockito.when(movieRepository.existsById("despicable+me+2+2013")).thenReturn(true);

            processor.process(watchMovie);

            Mockito.verify(userRepository, Mockito.times(0)).saveAndFlush(ArgumentMatchers.any(UserPo.class));
            Mockito.verify(movieRepository, Mockito.times(0)).saveAndFlush(ArgumentMatchers.any(MoviePo.class));
            Mockito.verify(watchingRepository, Mockito.times(1)).saveAndFlush(watchingPoArgumentCaptor.capture());

            WatchingPo watchingPo = watchingPoArgumentCaptor.getValue();
            assertEquals(watchingPo.getUserId(), 514537);
            assertEquals(watchingPo.getMovieTitle(), "despicable+me+2+2013");

        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    @DisplayName("NullPointerException occurs on user id that does not exist")
    void processInvalidUser() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);

        Mockito.when(userRepository.existsById(514537)).thenReturn(false);
        ResponseEntity<String> userInfo = ResponseEntity.ok(userNotFoundBody);
        Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/user/514537", String.class)).thenReturn(userInfo);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.process(watchMovie);
        });
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Process and parse correctly formatted rating log")
    void processRatingLog() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        try {
            ResponseEntity<String> userInfo = ResponseEntity.ok(userInfoBody);
            Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/user/514537", String.class)).thenReturn(userInfo);

            ResponseEntity<String> movieInfo = ResponseEntity.ok(movieInfoBody);
            Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/movie/despicable+me+2+2013", String.class)).thenReturn(movieInfo);

            processor.process(rateMovie);

            Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(userPoArgumentCaptor.capture());
            Mockito.verify(movieRepository, Mockito.times(1)).saveAndFlush(moviePoArgumentCaptor.capture());
            Mockito.verify(ratingRepository, Mockito.times(1)).saveAndFlush(ratingPoArgumentCaptor.capture());

            UserPo userPo = userPoArgumentCaptor.getValue();
            assertEquals(userPo.getId(), 514537);
            assertEquals(userPo.getAge(), 31);
            assertEquals(userPo.getGender(), "M");
            MoviePo moviePo = moviePoArgumentCaptor.getValue();
            assertEquals(moviePo.getTitle(), "despicable+me+2+2013");
            RatingPo ratingPo = ratingPoArgumentCaptor.getValue();
            assertEquals(ratingPo.getScore(), 4);
            assertEquals(ratingPo.getRatedAt(), LocalDateTime.parse("2022-03-04T20:16:46"));
        } catch (Exception e) {
            // do nothing. No exceptions should throw
        }
    }

    @Test
    @DisplayName("Process and parse correctly formatted rating log without creating new user and movie")
    void processRatingLogUserAndMovieExists() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        try {
            Mockito.when(userRepository.existsById(514537)).thenReturn(true);
            Mockito.when(movieRepository.existsById("despicable+me+2+2013")).thenReturn(true);

            processor.process(rateMovie);

            Mockito.verify(userRepository, Mockito.times(0)).saveAndFlush(ArgumentMatchers.any(UserPo.class));
            Mockito.verify(movieRepository, Mockito.times(0)).saveAndFlush(ArgumentMatchers.any(MoviePo.class));
            Mockito.verify(ratingRepository, Mockito.times(1)).saveAndFlush(ratingPoArgumentCaptor.capture());

            RatingPo ratingPo = ratingPoArgumentCaptor.getValue();
            assertEquals(ratingPo.getUserId(), 514537);
            assertEquals(ratingPo.getMovieTitle(), "despicable+me+2+2013");

        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    @DisplayName("NullPointerException occurs on movie title that does not exist")
    void processInvalidMovie() {
        Mockito.reset(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);

        Mockito.when(userRepository.existsById(514537)).thenReturn(true);
        Mockito.when(movieRepository.existsById("despicable+me+2+2013")).thenReturn(false);
        ResponseEntity<String> movieInfo = ResponseEntity.ok(movieNotFoundBody);
        Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/movie/despicable+me+2+2013", String.class)).thenReturn(movieInfo);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.process(rateMovie);
        });
        assertTrue(exception.getMessage().contains("null"));
    }
}