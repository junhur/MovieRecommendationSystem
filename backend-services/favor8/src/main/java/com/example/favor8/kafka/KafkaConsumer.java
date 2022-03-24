package com.example.favor8.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final KafkaProcessor kafkaProcessor;

    @KafkaListener(topics = "movielog8", groupId = "favor8", autoStartup = "false")
    public void consume(String message) {
//        log.info("Received message: " + message + " 🐯");

        if (message.split(",GET /").length <= 1) {
            try {
                kafkaProcessor.process_recommendation_request(message);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            log.info("Recommendation request: " + message + " 🐯");
            return;
        }

        try {
            kafkaProcessor.process(message);
        } catch (Exception e) {
            log.warn("user or movie not found: {}", message);
            log.warn("Exception message: {}", e.getMessage());
        }
    }
}
