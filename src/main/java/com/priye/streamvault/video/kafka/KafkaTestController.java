package com.priye.streamvault.video.kafka;

import com.priye.streamvault.video.kafka.VideoEvent;
import com.priye.streamvault.video.kafka.VideoEventProducer;
import com.priye.streamvault.video.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/test/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaTestController {

    private final VideoEventProducer videoEventProducer;
    private final VideoProcessingService videoProcessingService;

    @PostMapping("/duplicate/{videoId}")
    public void publishDuplicate(@PathVariable UUID videoId) {

        VideoEvent event1 = new VideoEvent(
                UUID.randomUUID(),
                "VIDEO_UPLOADED",
                videoId
        );

        VideoEvent event2 = new VideoEvent(
                UUID.randomUUID(),
                "VIDEO_UPLOADED",
                videoId
        );

        videoEventProducer.publishVideoUploaded(event1);
        videoEventProducer.publishVideoUploaded(event2);
    }

    @PostMapping("/concurrent-processing/{videoId}")
    public void concurrentProcessing(@PathVariable UUID videoId) {

        UUID event1 = UUID.randomUUID();
        UUID event2 = UUID.randomUUID();

        Runnable task1 = () -> {
            log.info("TEST-6 Thread-1 started. videoId={}, eventId={}", videoId, event1);

            videoProcessingService.processVideo(videoId, event1);

            log.info("TEST-6 Thread-1 finished. videoId={}, eventId={}", videoId, event1);
        };

        Runnable task2 = () -> {
            log.info("TEST-6 Thread-2 started. videoId={}, eventId={}", videoId, event2);

            videoProcessingService.processVideo(videoId, event2);

            log.info("TEST-6 Thread-2 finished. videoId={}, eventId={}", videoId, event2);
        };

        new Thread(task1).start();
        new Thread(task2).start();
    }
}