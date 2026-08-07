package com.priye.streamvault.video.dto.response;

import com.priye.streamvault.common.enums.VideoStatus;

import java.util.UUID;

public record VideoUploadResponse(
        UUID videoId,
        String fileName,
        Long fileSize,
        String contentType,
        VideoStatus status
) {
}