package com.priye.streamvault.video.kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Just to test the Kafka producer and consumer,
 * this configuration will send a test message to the Kafka topic when the application starts.
 */
@Configuration
public class KafkaTestConfig {

    @Bean
    CommandLineRunner kafkaTest(VideoEventProducer producer) {
        return args -> {

            UUID videoId = UUID.randomUUID();
            UUID eventId = UUID.randomUUID();


            VideoEvent event = new VideoEvent(
                    eventId,
                    "VIDEO_UPLOADED",
                    videoId
            );

//            producer.publishVideoUploaded(event);
        };
    }
}
