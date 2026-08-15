package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.video.dto.response.FFprobeResult;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.video.entity.VideoStatusTransition;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.repository.VideoStatusTransitionRepository;
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
    private final VideoStatusTransitionRepository transitionRepository;

    @Transactional
    @Override
    public void markReady(UUID videoId, UUID eventId, FFprobeResult result, String processedFilePath) {

        int updatedRows = videoRepository.markReady(
                videoId,
                eventId,
                VideoStatus.PROCESSING,
                VideoStatus.READY,
                result.duration(),
                result.width(),
                result.height(),
                result.videoCodec(),
                result.audioCodec(),
                processedFilePath
        );

        if (updatedRows != 1) {
            throw new IllegalStateException("Video processing claim no longer belongs to event. " + "videoId=" + videoId + ", eventId=" + eventId);
        }

        VideoStatusTransition transition = VideoStatusTransition.builder()
                .videoId(videoId)
                .fromStatus(VideoStatus.PROCESSING)
                .toStatus(VideoStatus.READY)
                .eventId(eventId)
                .reason("Video processing completed successfully")
                .build();

        transitionRepository.save(transition);

        log.info("Video marked READY. videoId={}, eventId={}", videoId, eventId);

    }

    @Transactional
    @Override
    public void markFailed(UUID videoId, UUID eventId) {

        int updatedRows = videoRepository.markFailed(
                videoId,
                eventId,
                VideoStatus.PROCESSING,
                VideoStatus.FAILED
        );

        if (updatedRows == 1) {

            VideoStatusTransition transition = VideoStatusTransition.builder()
                    .videoId(videoId)
                    .fromStatus(VideoStatus.PROCESSING)
                    .toStatus(VideoStatus.FAILED)
                    .eventId(eventId)
                    .reason("Video processing failed after Kafka retries were exhausted")
                    .build();

            transitionRepository.save(transition);

            log.info("Video marked as FAILED. videoId={}, eventId={}", videoId, eventId);

        } else {

            log.warn("Video was not marked as FAILED because the processing claim did not match. videoId={}, eventId={}", videoId, eventId);

        }
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

        log.info("Claim processing result. videoId={}, eventId={}, updatedRows={}", videoId, eventId, updatedRows);

        if (updatedRows == 1) {

            VideoStatusTransition transition = VideoStatusTransition.builder()
                    .videoId(videoId)
                    .fromStatus(VideoStatus.UPLOADED)
                    .toStatus(VideoStatus.PROCESSING)
                    .eventId(eventId)
                    .reason("Video processing started")
                    .build();

            transitionRepository.save(transition);

            log.info("Video status transition recorded. videoId={}, {} -> {}, eventId={}",
                    videoId,
                    VideoStatus.UPLOADED,
                    VideoStatus.PROCESSING,
                    eventId
            );
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public void resetProcessingToUploaded(UUID videoId, UUID eventId) {

        int updatedRows = videoRepository.resetProcessingToUploaded(
                videoId,
                eventId,
                VideoStatus.PROCESSING,
                VideoStatus.UPLOADED
        );

        log.info("Reset processing video to uploaded. videoId={}, eventId={}, updatedRows={}", videoId, eventId, updatedRows);

    }

}
