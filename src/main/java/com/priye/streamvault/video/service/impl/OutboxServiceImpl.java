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

        VideoEvent event = new VideoEvent("VIDEO_UPLOADED", videoId);

        String payload = objectMapper.writeValueAsString(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(event.eventType())
                .aggregateId(videoId)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
