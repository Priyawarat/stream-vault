package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.OutboxStatus;
import com.priye.streamvault.video.entity.OutboxEvent;
import com.priye.streamvault.video.kafka.VideoEvent;
import com.priye.streamvault.video.repository.OutboxEventRepository;
import com.priye.streamvault.video.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void saveVideoUploadedEvent(UUID videoId) {

        UUID eventId = UUID.randomUUID();

        VideoEvent event = new VideoEvent(
                eventId,
                "VIDEO_UPLOADED",
                videoId
        );

        String payload = objectMapper.writeValueAsString(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .eventType("VIDEO_UPLOADED")
                .aggregateId(videoId)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
