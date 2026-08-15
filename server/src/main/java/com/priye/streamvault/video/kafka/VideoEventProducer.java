package com.priye.streamvault.video.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class VideoEventProducer {

    private static final String TOPIC = "video-uploaded";

    private final KafkaTemplate<String, VideoEvent> kafkaTemplate;

    public CompletableFuture<?> publishVideoUploaded(VideoEvent event) {
        return kafkaTemplate.send(TOPIC, event.videoId().toString(), event);
    }
}
