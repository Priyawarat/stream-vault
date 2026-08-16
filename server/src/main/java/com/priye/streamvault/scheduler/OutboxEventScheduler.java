package com.priye.streamvault.scheduler;

import com.priye.streamvault.common.enums.OutboxStatus;
import com.priye.streamvault.messaging.outbox.entity.OutboxEvent;
import com.priye.streamvault.messaging.kafka.VideoEvent;
import com.priye.streamvault.messaging.kafka.VideoEventProducer;
import com.priye.streamvault.messaging.outbox.repository.OutboxEventRepository;
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

    private static final int MAX_RETRY_COUNT = 3;
    private static final int STUCK_EVENT_TIMEOUT_MINUTES = 5;

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {

        log.info("Outbox scheduler triggered");

        recoverStuckEvents();

        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : events) {
            try {

                // Claim event: PENDING → PROCESSING
                int updatedRows = outboxEventRepository.claimEvent(event.getId(), OutboxStatus.PENDING, OutboxStatus.PROCESSING, LocalDateTime.now());

                if (updatedRows == 0) {
                    log.info("Skipping outbox event because it was already claimed. eventId={}", event.getId());
                    continue;
                }

                log.info("Outbox event claimed. eventId={}, videoId={}, status=PROCESSING", event.getId(), event.getAggregateId());

                // Deserialize event
                VideoEvent videoEvent = objectMapper.readValue(event.getPayload(), VideoEvent.class);

                // Publish to Kafka
                videoEventProducer.publishVideoUploaded(videoEvent).get();

                // Kafka success: PROCESSING → PROCESSED
                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                event.setProcessingAt(null);

                outboxEventRepository.save(event);

                log.info("Outbox event processed successfully. eventId={}, videoId={}", event.getId(), event.getAggregateId());

            } catch (Exception e) {
                log.error("Failed to process outbox event with ID {}: {}", event.getId(), e.getMessage());

                // Kafka failed: PROCESSING → PENDING // The next scheduler execution will retry it.
                handleFailure(event, e);
            }
        }
    }

    private void handleFailure(OutboxEvent event, Exception exception) {

        int nextRetryCount = event.getRetryCount() + 1;

        event.setRetryCount(nextRetryCount);
        event.setLastError(getErrorMessage(exception));
        event.setProcessingAt(null);

        if (nextRetryCount >= MAX_RETRY_COUNT) {

            event.setStatus(OutboxStatus.FAILED);

            outboxEventRepository.save(event);

            log.error("Outbox event permanently FAILED after {} attempts. eventId={}, videoId={}, error={}",
                    nextRetryCount,
                    event.getId(),
                    event.getAggregateId(),
                    event.getLastError()
            );

        } else {

            event.setStatus(OutboxStatus.PENDING);

            outboxEventRepository.save(event);

            log.warn(
                    "Outbox event failed. Returning to PENDING for retry. " + "eventId={}, videoId={}, retryCount={}, maxRetries={}, error={}",
                    event.getId(),
                    event.getAggregateId(),
                    nextRetryCount,
                    MAX_RETRY_COUNT,
                    event.getLastError()
            );
        }
    }

    private String getErrorMessage(Exception exception) {

        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
    }

    private void recoverStuckEvents() {

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(STUCK_EVENT_TIMEOUT_MINUTES);

        int recovered = outboxEventRepository.recoverStuckEvents(OutboxStatus.PROCESSING, OutboxStatus.PENDING, cutoffTime);

        if (recovered > 0) {
            log.warn("Recovered {} stuck outbox event(s) back to PENDING", recovered);
        }
    }
}
