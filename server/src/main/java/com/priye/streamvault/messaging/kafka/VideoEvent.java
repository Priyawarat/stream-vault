package com.priye.streamvault.messaging.kafka;

import java.util.UUID;

public record VideoEvent(
        UUID eventId,
        String eventType,
        UUID videoId
) {
}
