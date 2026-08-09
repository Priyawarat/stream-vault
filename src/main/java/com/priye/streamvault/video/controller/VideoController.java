package com.priye.streamvault.video.controller;

import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoStreamData;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;
import com.priye.streamvault.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@RestController
@RequestMapping("/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // TODO: replace with authenticated user context
    private final UUID userId = UUID.fromString("455fd79d-bf31-42df-94b9-fdd9d9bbe38a");

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<VideoUploadResponse> upload(@RequestParam("file") MultipartFile file) {

        VideoUploadRequest request = new VideoUploadRequest(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(videoService.upload(userId, request));
    }

    @GetMapping("/{videoId}/stream-full")
    public ResponseEntity<Resource> stream(@PathVariable UUID videoId) {
        VideoStreamData data = videoService.stream(videoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(data.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + data.originalFileName() + "\"")
                .body(data.resource());
    }

    @GetMapping("/{videoId}/stream")
    public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable UUID videoId, @RequestHeader(value = HttpHeaders.RANGE, required = false) String range) {
        return videoService.stream(videoId, range);
    }
}