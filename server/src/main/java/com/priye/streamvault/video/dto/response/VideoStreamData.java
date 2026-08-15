package com.priye.streamvault.video.dto.response;

import org.springframework.core.io.Resource;

public record VideoStreamData(
        Resource resource,
        String contentType,
        String originalFileName
) {}
