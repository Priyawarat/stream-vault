package com.priye.streamvault.video.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoEventConsumer {

    @KafkaListener(topics = "video-uploaded", groupId = "streamvault-video-processor")
    public void consume(VideoEvent event) {

        log.info("Received video event: eventType={}, videoId={}", event.eventType(), event.videoId());
    }
}
