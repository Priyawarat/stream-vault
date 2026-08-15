package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoListResponse;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

public interface VideoService {

    VideoUploadResponse upload(UUID userId, VideoUploadRequest request);

    ResponseEntity<StreamingResponseBody> stream(UUID videoId, String range);

    List<VideoListResponse> getAllVideos();

    ResponseEntity<Resource> getThumbnail(UUID videoId);
}