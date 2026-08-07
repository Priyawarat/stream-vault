package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;

import java.util.UUID;

public interface VideoService {

    VideoUploadResponse upload(UUID userId, VideoUploadRequest request);
}