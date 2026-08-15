package com.priye.streamvault.video.kafka;

import java.util.UUID;

public record VideoEvent(
        UUID eventId,
        String eventType,
        UUID videoId
) {
}
