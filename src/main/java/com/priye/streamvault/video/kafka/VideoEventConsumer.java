package com.priye.streamvault.video.kafka;

import com.priye.streamvault.video.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoEventConsumer {

    private final VideoProcessingService videoProcessingService;

    @KafkaListener(topics = "video-uploaded", groupId = "streamvault-video-processor")
    public void consume(VideoEvent event) {

        log.info("Received video event: eventType={}, videoId={}", event.eventType(), event.videoId());

        if ("VIDEO_UPLOADED".equals(event.eventType())) {
            videoProcessingService.processVideo(event.videoId());
        }
    }
}
