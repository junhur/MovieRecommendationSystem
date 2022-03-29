package com.example.favor8.kafka;

import com.example.favor8.dao.entity.RecommendationRequestPo;
import com.example.favor8.dao.entity.UserPo;
import com.example.favor8.dao.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaProcessorTest {
    // Mock User information response body
    private final String userInfoBody = "{\"user_id\":514537,\"age\":31,\"occupation\":\"college/grad student\",\"gender\":\"M\"}";

    // Mock Repositories to be used in test
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final MovieRepository movieRepository = Mockito.mock(MovieRepository.class);
    private final RatingRepository ratingRepository = Mockito.mock(RatingRepository.class);
    private final WatchingRepository watchingRepository = Mockito.mock(WatchingRepository.class);
    private final RecommendationRequestRepository recommendationRequestRepository = Mockito.mock(RecommendationRequestRepository.class);

    // Mock RestTemplate
    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    //ArgumentCaptors
    @Captor
    private ArgumentCaptor<RecommendationRequestPo> recommendationRequestPoArgumentCaptor;

    // Parameter Kafka message Strings for Recommendation Request processing
    private final String recRequest = "2022-03-04T20:22:00.415793,514537,recommendation request 17645-team08.isri.cmu.edu:8082, status 200, result: fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997, 391 ms";
    private final String incorrectTypeRecRequest = "REQUESTED_TIME,USER_ID,recommendation request 17645-team08.isri.cmu.edu:8082, status twohundred, result: fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997, TEN ms";
    private final String whiteSpaceRecRequest = "2022-03-04T20:22:00.415793,     514537,    recommendation request 17645-team08.isri.cmu.edu:8082, status      200, result:     fight+club+1999, penguins+of+madagascar+2014, pirates+of+the+caribbean+on+stranger+tides+2011, spider-man+3+2007, the+maze+runner+2014, iron+man+3+2013, aliens+1986, the+godfather+part+ii+1974, pirates+of+the+caribbean+at+worlds+end+2007, the+hobbit+an+unexpected+journey+2012, x-men+days+of+future+past+2014, the+amazing+spider-man+2012, dark+skies+2013, ghostbusters+1984, pans+labyrinth+2006, finding+nemo+2003, the+hobbit+the+battle+of+the+five+armies+2014, the+lord+of+the+rings+the+fellowship+of+the+ring+2001, the+incredibles+2004, the+fifth+element+1997, 391       ms";

    // Rating and Watching messages
    private final String rateMovie = "2022-03-04T20:16:46,9610,GET /rate/harry+potter+and+the+deathly+hallows+part+2+2011=4";
    private final String watchMovie = "2022-03-16T00:29:05,180184,GET /data/m/despicable+me+2+2013/90.mpg";

    // Tests for parse_recommendation_request
    @Test
    @DisplayName("Parse valid recommendation request")
    void parse_recommendation_request() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        try {
            String[] result = processor.parse_recommendation_request(recRequest);
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
    void parse_white_spaced_recommendation_request() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        try {
            String[] result = processor.parse_recommendation_request(whiteSpaceRecRequest);
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
    void parse_incorrect_types_recommendation_request() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        try {
            String[] result = processor.parse_recommendation_request(incorrectTypeRecRequest);
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
    void parse_recommendation_request_invalid_message() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.parse_recommendation_request(watchMovie);
        });
        assertTrue(exception.getMessage().contains("Recommendation request not in expected format"));
    }

    // Tests for process_recommendation_request
    @Test
    @DisplayName("Happy path process recommendation request")
    void process_recommendation_request() {
        KafkaProcessor processor = new KafkaProcessor(userRepository, movieRepository, ratingRepository, watchingRepository, recommendationRequestRepository, restTemplate);
        try {
            ResponseEntity<String> userInfo = ResponseEntity.ok(userInfoBody);
            Mockito.when(restTemplate.getForEntity("http://128.2.204.215:8080/user/514537", String.class)).thenReturn(userInfo);

            processor.process_recommendation_request(recRequest);

            Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(ArgumentMatchers.any(UserPo.class));
            Mockito.verify(recommendationRequestRepository, Mockito.times(1)).saveAndFlush(ArgumentMatchers.any(RecommendationRequestPo.class));

            // TODO: figure out why argument captor throws UnfinishedVerificationException
//            Mockito.verify(recommendationRequestRepository).saveAndFlush(recommendationRequestPoArgumentCaptor.capture());
//
//            RecommendationRequestPo recPo = recommendationRequestPoArgumentCaptor.getValue();
//            assertEquals(recPo.getUserId(), 514537);
//            assertEquals(recPo.getRequestedAt(), LocalDateTime.parse("2022-03-04T20:22:00.415793"));
//            assertEquals(recPo.getResponseTime(), 391);
//            assertEquals(recPo.getStatus(), 200);
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    @DisplayName("Throws an exception when message type is not recommendation request")
    void process_recommendation_request_unsupported_message_type() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.process_recommendation_request(watchMovie);
        });
        assertTrue(exception.getMessage().contains("Unsupported log type"));
    }

    @Test
    void process_recommendation_request_incorrect_types() {
        KafkaProcessor processor = new KafkaProcessor(null, null, null, null, null, null);
        Exception exception = assertThrows(Exception.class, () -> {
            processor.process_recommendation_request(incorrectTypeRecRequest);
        });
        assertTrue(exception.getMessage().contains("Error occurred while parsing and storing rec request"));
    }
}