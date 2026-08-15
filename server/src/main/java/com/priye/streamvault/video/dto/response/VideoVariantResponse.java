package com.priye.streamvault.video.dto.response;

import com.priye.streamvault.common.enums.VideoVariantStatus;

import java.util.UUID;

public record VideoVariantResponse(
        UUID id,
        String resolution,
        Long fileSize,
        Long bitrate,
        VideoVariantStatus status
) {
}