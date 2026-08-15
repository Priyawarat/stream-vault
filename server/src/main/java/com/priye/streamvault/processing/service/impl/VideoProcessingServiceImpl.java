package com.priye.streamvault.processing.service.impl;

import com.priye.streamvault.common.enums.VideoProcessingJobStatus;
import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.processing.ffmpeg.FFmpegService;
import com.priye.streamvault.processing.ffprobe.FFprobeService;
import com.priye.streamvault.processing.service.VideoProcessingService;
import com.priye.streamvault.processing.thumbnail.ThumbnailService;
import com.priye.streamvault.processing.variant.VideoVariantService;
import com.priye.streamvault.video.dto.response.FFprobeResult;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.processing.entity.VideoProcessingJob;
import com.priye.streamvault.processing.repository.VideoProcessingJobRepository;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final VideoRepository videoRepository;
    private final FFprobeService ffprobeService;
    private final FFmpegService ffmpegService;
    private final VideoStatusService videoStatusService;
    private final VideoProcessingJobRepository videoProcessingJobRepository;
    private final ThumbnailService thumbnailService;
    private final VideoVariantService videoVariantService;

    @Override
    public void processVideo(UUID videoId, UUID eventId) {

        log.info("Starting video processing. videoId={}, eventId={}", videoId, eventId);

        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                        new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));

        boolean claimed = videoStatusService.claimProcessing(videoId, eventId);

        if (!claimed) {
            log.info("Skipping video processing. videoId={}, eventId={}, currentStatus={}, processingEventId={}",
                    videoId,
                    eventId,
                    video.getStatus(),
                    video.getProcessingEventId()
            );
            return;
        }

        VideoProcessingJob job = VideoProcessingJob.builder()
                .videoId(videoId)
                .eventId(eventId)
                .status(VideoProcessingJobStatus.PROCESSING)
                .startedAt(LocalDateTime.now())
                .build();

        job = videoProcessingJobRepository.save(job);

        video = videoRepository.findById(videoId).orElseThrow(() ->
                new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));

        log.info("Video processing claimed successfully. videoId={}, eventId={}, status={}",
                videoId,
                eventId,
                video.getStatus()
        );

        try {

            // FFprobe
            FFprobeResult result = ffprobeService.probe(video.getStoragePath());

            log.info("FFprobe completed successfully. videoId={}, duration={}, resolution={}x{}, videoCodec={}, audioCodec={}",
                    videoId,
                    result.duration(),
                    result.width(),
                    result.height(),
                    result.videoCodec(),
                    result.audioCodec()
            );

            // FFmpeg
            String inputPath = video.getStoragePath();
            String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "_processed.mp4";

            log.info("Starting FFmpeg. videoId={}, input={}, output={}", videoId, inputPath, outputPath);
            ffmpegService.process(inputPath, outputPath);

            // Video Variants
            videoVariantService.generateVariants(videoId, outputPath, result);

            // Thumbnail
            String thumbnailPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "_thumbnail.jpg";

            log.info("Starting thumbnail generation. videoId={}, input={}, thumbnail={}", videoId, outputPath, thumbnailPath);

            thumbnailService.generate(outputPath, thumbnailPath);

            // Transaction 2: PROCESSING → READY → COMMIT
            videoStatusService.markReady(videoId, eventId, result, outputPath, thumbnailPath);

            log.info("Video processing completed successfully. videoId={}, status={}", videoId, VideoStatus.READY);

            job.setStatus(VideoProcessingJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            videoProcessingJobRepository.save(job);

        } catch (Exception e) {

            log.error("Video processing failed. videoId={}, eventId={}", videoId, eventId, e);

            /*
             * IMPORTANT:
             *
             * Do NOT reset PROCESSING → UPLOADED here.
             *
             * Kafka will retry the same event.
             *
             * The video remains:
             *
             * PROCESSING
             * processingEventId = eventId
             *
             * If all Kafka retries fail, the DLQ recoverer
             * will mark this exact processing attempt as FAILED.
             */
            job.setStatus(VideoProcessingJobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorMessage(e.getMessage());

            videoProcessingJobRepository.save(job);

            throw e;
        }

    }

}
