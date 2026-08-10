package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final VideoRepository videoRepository;

    @Transactional
    @Override
    public void processVideo(UUID videoId) {

        log.info("Starting video processing. videoId={}", videoId);

        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                        new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));

        if (video.getStatus() != VideoStatus.UPLOADED) {
            log.warn("Skipping video processing. videoId={}, currentStatus={}", videoId, video.getStatus());
            return;
        }

        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);

        log.info("Video status changed successfully. videoId={}, status={}", videoId, VideoStatus.PROCESSING);

    }

}
