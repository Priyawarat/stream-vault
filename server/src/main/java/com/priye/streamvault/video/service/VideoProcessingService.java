package com.priye.streamvault.video.service;

import java.util.UUID;

public interface VideoProcessingService {

    void processVideo(UUID videoId, UUID eventId);

}
