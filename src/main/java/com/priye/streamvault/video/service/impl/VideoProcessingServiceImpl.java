package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.video.dto.response.FFprobeResult;
import com.priye.streamvault.video.entity.Video;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.video.service.FFmpegService;
import com.priye.streamvault.video.service.FFprobeService;
import com.priye.streamvault.video.service.VideoProcessingService;
import com.priye.streamvault.video.service.VideoStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final VideoRepository videoRepository;
    private final FFprobeService ffprobeService;
    private final FFmpegService ffmpegService;
    private final VideoStatusService videoStatusService;

    @Override
    public void processVideo(UUID videoId) {

        log.info("Starting video processing. videoId={}", videoId);

        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                        new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId));

        if (video.getStatus() != VideoStatus.UPLOADED && video.getStatus() != VideoStatus.PROCESSING) {
            log.warn("Skipping video processing. videoId={}, currentStatus={}", videoId, video.getStatus());
            return;
        }

        // First attempt: UPLOADED → PROCESSING → COMMIT
        if (video.getStatus() == VideoStatus.UPLOADED) {

            videoStatusService.markProcessing(videoId);

            log.info("Video status changed successfully. videoId={}, status={}", videoId, VideoStatus.PROCESSING);
        }

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

            // Transaction 2: PROCESSING → READY → COMMIT
            videoStatusService.markReady(videoId, result, outputPath);

            log.info("Video processing completed successfully. videoId={}, status={}", videoId, VideoStatus.READY);

        } catch (Exception e) {

            log.error("Video processing failed. videoId={}", videoId, e);

            // Do NOT mark FAILED here.
            // Kafka must receive the exception so that it can retry.

            throw e;
        }

    }

}
