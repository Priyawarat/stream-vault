package com.priye.streamvault.video.service;

import java.util.UUID;

public interface OutboxService {

    void saveVideoUploadedEvent(UUID videoId);
}
