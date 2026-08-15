package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoListResponse;
import com.priye.streamvault.video.dto.response.VideoStreamData;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.service.OutboxService;
import com.priye.streamvault.video.service.StorageService;
import com.priye.streamvault.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final StorageService storageService;
    private final OutboxService outboxService;

    @Transactional
    @Override
    public VideoUploadResponse upload(UUID userId, VideoUploadRequest request) {

        MultipartFile file = request.file();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file cannot be empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("video/")) {
            throw new IllegalArgumentException("Only video files are allowed");
        }

        try {

            String storagePath = storageService.store(file);

            Path path = Paths.get(storagePath);
            String storedFileName = path.getFileName().toString();

            Video video = Video.builder()
                    .userId(userId)
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .storagePath(storagePath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .status(VideoStatus.UPLOADED)
                    .build();

            video = videoRepository.save(video);

            // Save event in the SAME DB transaction
            outboxService.saveVideoUploadedEvent(video.getId());

            return new VideoUploadResponse(
                    video.getId(),
                    video.getOriginalFileName(),
                    video.getFileSize(),
                    video.getContentType(),
                    video.getStatus()
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to store video", e);
        }
    }

    @Override
    public VideoStreamData stream(UUID videoId) {

        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));

        Resource resource = storageService.load(video.getStoragePath());

        return new VideoStreamData(resource, video.getContentType(), video.getOriginalFileName());
    }

    @Override
    public ResponseEntity<StreamingResponseBody> stream(UUID videoId, String range) {

        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));

        if (video.getStatus() != VideoStatus.READY) {
            throw new IllegalStateException("Video is not ready for streaming");
        }

        Resource resource = storageService.load(video.getProcessedFilePath());

        try {

            long fileSize = resource.contentLength();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(video.getContentType()));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            if (range == null || range.isBlank()) {

                headers.setContentLength(fileSize);

                StreamingResponseBody responseBody = outputStream -> {

                    try (var inputStream = resource.getInputStream()) {

                        byte[] buffer = new byte[8192];

                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.flush();
                    }
                };

                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(responseBody);
            }

            // Range header exists
            List<HttpRange> ranges = HttpRange.parseRanges(range);

            if (ranges.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            // For now, support only one range
            HttpRange httpRange = ranges.get(0);

            long start = httpRange.getRangeStart(fileSize);
            long end = httpRange.getRangeEnd(fileSize);

            if (start >= fileSize || start > end) {

                return ResponseEntity
                        .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            long contentLength = end - start + 1;

            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
            headers.setContentLength(contentLength);

            StreamingResponseBody responseBody = outputStream -> {

                try (var inputStream = resource.getInputStream()) {
                    long bytesSkipped = 0;
                    while (bytesSkipped < start) {

                        long skipped = inputStream.skip(start - bytesSkipped);

                        if (skipped <= 0) {
                            break;
                        }

                        bytesSkipped += skipped;
                    }

                    byte[] buffer = new byte[8192];

                    long remaining = contentLength;

                    while (remaining > 0) {

                        int bytesToRead =
                                (int) Math.min(buffer.length, remaining);

                        int bytesRead =
                                inputStream.read(buffer, 0, bytesToRead);

                        if (bytesRead == -1) {
                            break;
                        }

                        outputStream.write(buffer, 0, bytesRead);

                        remaining -= bytesRead;
                    }
                    outputStream.flush();
                }
            };

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(responseBody);

        } catch (IOException e) {
            throw new RuntimeException("Failed to stream video", e);
        }
    }

    @Override
    public List<VideoListResponse> getAllVideos() {

        List<VideoListResponse> videoListResponses = videoRepository.findByStatus(VideoStatus.READY)
                .stream()
                .map(video -> new VideoListResponse(
                        video.getId(),
                        video.getOriginalFileName(),
                        video.getFileSize(),
                        video.getContentType(),
                        video.getStatus(),
                        video.getCreatedAt()
                ))
                .toList();

        return videoListResponses;
    }
}