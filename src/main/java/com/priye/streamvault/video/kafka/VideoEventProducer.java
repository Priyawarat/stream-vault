package com.priye.streamvault.video.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoEventProducer {

    private static final String TOPIC = "video-uploaded";

    private final KafkaTemplate<String, VideoEvent> kafkaTemplate;

    public void publishVideoUploaded(VideoEvent event) {

        kafkaTemplate.send(TOPIC, event.videoId().toString(), event);
    }
}
