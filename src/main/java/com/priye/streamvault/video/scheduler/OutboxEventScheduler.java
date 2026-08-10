package com.priye.streamvault.video.scheduler;

import com.priye.streamvault.common.enums.OutboxStatus;
import com.priye.streamvault.video.entity.OutboxEvent;
import com.priye.streamvault.video.kafka.VideoEvent;
import com.priye.streamvault.video.kafka.VideoEventProducer;
import com.priye.streamvault.video.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final VideoEventProducer videoEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {

        log.info("Outbox scheduler triggered");

        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : events) {
            try {
                VideoEvent videoEvent = objectMapper.readValue(event.getPayload(), VideoEvent.class);

                videoEventProducer.publishVideoUploaded(videoEvent).get();

                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());

                outboxEventRepository.save(event);

                log.info("Outbox event processed successfully. eventId={}, videoId={}", event.getId(), event.getAggregateId());

            } catch (Exception e) {
                log.error("Failed to process outbox event with ID {}: {}", event.getId(), e.getMessage());
                // Keep event as PENDING so the next scheduler run retries it
            }
        }
    }
}
