package com.priye.streamvault.video.controller;

import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;
import com.priye.streamvault.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // TODO: replace with authenticated user context
    private final UUID userId = UUID.fromString("268e9972-8452-4dfb-b161-b4c3bf118e44");

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<VideoUploadResponse> upload(@RequestParam("file") MultipartFile file) {

        VideoUploadRequest request = new VideoUploadRequest(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(videoService.upload(userId, request));
    }
}