package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoStreamData;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

public interface VideoService {

    VideoUploadResponse upload(UUID userId, VideoUploadRequest request);

    VideoStreamData stream(UUID videoId);

    ResponseEntity<StreamingResponseBody> stream(UUID videoId, String range);
}