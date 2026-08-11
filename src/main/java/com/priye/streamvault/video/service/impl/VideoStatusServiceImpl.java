package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.video.dto.response.FFprobeResult;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.service.VideoStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoStatusServiceImpl implements VideoStatusService {

    private final VideoRepository videoRepository;

    @Transactional
    @Override
    public void markProcessing(UUID videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));
        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);
    }

    @Transactional
    @Override
    public void markReady(UUID videoId, FFprobeResult result, String processedFilePath) {
        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));
        video.setStatus(VideoStatus.READY);
        video.setDuration(result.duration());
        video.setWidth(result.width());
        video.setHeight(result.height());
        video.setVideoCodec(result.videoCodec());
        video.setAudioCodec(result.audioCodec());
        video.setProcessedFilePath(processedFilePath);
        videoRepository.save(video);
    }

    @Transactional
    @Override
    public void markFailed(UUID videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));
        video.setStatus(VideoStatus.FAILED);
        videoRepository.save(video);
    }

    @Transactional
    @Override
    public boolean claimProcessing(UUID videoId, UUID eventId) {

        int updatedRows = videoRepository.claimProcessing(
                videoId,
                eventId,
                VideoStatus.UPLOADED,
                VideoStatus.PROCESSING
        );

        return updatedRows == 1;
    }
}
