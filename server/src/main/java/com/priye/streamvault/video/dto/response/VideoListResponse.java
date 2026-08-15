package com.priye.streamvault.video.dto.response;

import com.priye.streamvault.common.enums.VideoStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoListResponse(
        UUID videoId,
        String fileName,
        Long fileSize,
        String contentType,
        VideoStatus status,
        LocalDateTime createdAt
) {
}
