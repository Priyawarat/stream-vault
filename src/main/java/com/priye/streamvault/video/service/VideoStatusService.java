package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.response.FFprobeResult;

import java.util.UUID;

public interface VideoStatusService {

    void markFailed(UUID videoId, UUID eventId) ;

    void markReady(UUID videoId, UUID eventId, FFprobeResult result, String processedFilePath);

    boolean claimProcessing(UUID videoId, UUID eventId);

    void resetProcessingToUploaded(UUID videoId, UUID eventId);
}