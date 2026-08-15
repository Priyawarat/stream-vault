package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.response.FFprobeResult;
import com.priye.streamvault.video.dto.response.VideoVariantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

public interface VideoVariantService {

    List<UUID> generateVariants(UUID videoId, String inputPath, FFprobeResult probeResult);

    List<VideoVariantResponse> getVariants(UUID videoId);

    ResponseEntity<StreamingResponseBody> streamVariant(UUID videoId, String resolution, String range);
}