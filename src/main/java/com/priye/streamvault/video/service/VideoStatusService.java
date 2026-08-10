package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.response.FFprobeResult;

import java.util.UUID;

public interface VideoStatusService {

    void markProcessing(UUID videoId);

    void markFailed(UUID videoId) ;

    void markReady(UUID videoId, FFprobeResult result);
}