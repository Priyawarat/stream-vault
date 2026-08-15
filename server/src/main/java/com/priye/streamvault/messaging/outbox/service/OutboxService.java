package com.priye.streamvault.messaging.outbox.service;

import java.util.UUID;

public interface OutboxService {

    void saveVideoUploadedEvent(UUID videoId);
}
