package com.priye.streamvault.video.kafka;

import java.util.UUID;

public record VideoEvent(
        String eventType,
        UUID videoId
) {
}
