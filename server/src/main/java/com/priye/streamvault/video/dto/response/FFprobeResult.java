package com.priye.streamvault.video.dto.response;

public record FFprobeResult(
        double duration,
        int width,
        int height,
        String videoCodec,
        String audioCodec
) {
}