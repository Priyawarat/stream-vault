package com.priye.streamvault.processing.service;

import java.util.UUID;

public interface VideoProcessingService {

    void processVideo(UUID videoId, UUID eventId);

}
