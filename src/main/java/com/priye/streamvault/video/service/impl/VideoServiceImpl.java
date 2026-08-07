package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.video.dto.request.VideoUploadRequest;
import com.priye.streamvault.video.dto.response.VideoUploadResponse;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.service.StorageService;
import com.priye.streamvault.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final StorageService storageService;

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
}