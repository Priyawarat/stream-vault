package com.priye.streamvault.processing.variant;

import com.priye.streamvault.common.enums.VideoVariantStatus;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.video.dto.response.FFprobeResult;
import com.priye.streamvault.video.dto.response.VideoVariantResponse;
import com.priye.streamvault.processing.entity.VideoVariant;
import com.priye.streamvault.video.repository.VideoRepository;
import com.priye.streamvault.processing.repository.VideoVariantRepository;
import com.priye.streamvault.processing.ffmpeg.FFmpegService;
import com.priye.streamvault.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoVariantServiceImpl implements VideoVariantService {

    private final FFmpegService ffmpegService;
    private final VideoVariantRepository videoVariantRepository;
    private final VideoRepository videoRepository;
    private final StorageService storageService;

    @Override
    public List<UUID> generateVariants(UUID videoId, String inputPath, FFprobeResult probeResult) {

        log.info("Starting video variant generation. videoId={}, sourceHeight={}", videoId, probeResult.height());

        int sourceHeight = probeResult.height();

        List<VariantDefinition> variants = new ArrayList<>();

        if (sourceHeight >= 360) {
            variants.add(new VariantDefinition("360p", 360, 800));
        }

        if (sourceHeight >= 480) {
            variants.add(new VariantDefinition("480p", 480, 1400));
        }

        if (sourceHeight >= 720) {
            variants.add(new VariantDefinition("720p", 720, 2500));
        }

        List<UUID> variantIds = new ArrayList<>();

        for (VariantDefinition variant : variants) {

            String outputPath = buildOutputPath(
                    inputPath,
                    variant.resolution()
            );

            log.info("Generating variant. videoId={}, resolution={}, output={}", videoId, variant.resolution(), outputPath);

            ffmpegService.generateVariant(inputPath, outputPath, variant.height(), variant.bitrate());

            File file = new File(outputPath);

            if (!file.exists()) {
                throw new RuntimeException("Generated variant file does not exist: " + outputPath);
            }

            VideoVariant videoVariant = VideoVariant.builder()
                    .videoId(videoId)
                    .resolution(variant.resolution())
                    .storagePath(outputPath)
                    .fileSize(file.length())
                    .bitrate(variant.bitrate())
                    .status(VideoVariantStatus.READY)
                    .build();

            videoVariant = videoVariantRepository.save(videoVariant);

            variantIds.add(videoVariant.getId());

            log.info("Video variant saved successfully. videoId={}, variantId={}, resolution={}", videoId,
                    videoVariant.getId(),
                    variant.resolution()
            );
        }

        log.info("Video variant generation completed. videoId={}, variants={}", videoId, variantIds.size());

        return variantIds;
    }

    @Override
    public List<VideoVariantResponse> getVariants(UUID videoId) {

        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId);
        }

        return videoVariantRepository
                .findByVideoIdOrderByBitrateAsc(videoId)
                .stream()
                .map(variant -> new VideoVariantResponse(
                        variant.getId(),
                        variant.getResolution(),
                        variant.getFileSize(),
                        variant.getBitrate(),
                        variant.getStatus()
                ))
                .toList();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> streamVariant(UUID videoId, String resolution, String range) {

        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("VIDEO_NOT_FOUND", "Video not found with id: " + videoId);
        }

        VideoVariant variant = videoVariantRepository.findByVideoIdAndResolution(videoId, resolution)
                .orElseThrow(() -> new ResourceNotFoundException("VIDEO_VARIANT_NOT_FOUND", "Video variant not found. videoId=" + videoId +
                                ", resolution=" + resolution));

        if (variant.getStatus() != VideoVariantStatus.READY) {
            throw new IllegalStateException("Video variant is not ready for streaming");
        }

        Resource resource = storageService.load(variant.getStoragePath());

        try {

            long fileSize = resource.contentLength();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            // No Range header
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

                return ResponseEntity.ok().headers(headers).body(responseBody);
            }

            // Range header exists
            List<HttpRange> ranges = HttpRange.parseRanges(range);

            if (ranges.isEmpty()) {

                return ResponseEntity
                        .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(
                                HttpHeaders.CONTENT_RANGE,
                                "bytes */" + fileSize
                        )
                        .build();
            }

            // Simple implementation: support only one range
            HttpRange httpRange = ranges.get(0);

            long start = httpRange.getRangeStart(fileSize);
            long end = httpRange.getRangeEnd(fileSize);

            if (start >= fileSize || start > end) {

                return ResponseEntity
                        .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(
                                HttpHeaders.CONTENT_RANGE,
                                "bytes */" + fileSize
                        )
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

                        int bytesToRead = (int) Math.min(buffer.length, remaining);

                        int bytesRead = inputStream.read(buffer, 0, bytesToRead);

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
            throw new RuntimeException("Failed to stream video variant", e);
        }
    }

    private String buildOutputPath(String inputPath, String resolution) {

        int extensionIndex = inputPath.lastIndexOf('.');

        String basePath = extensionIndex >= 0 ? inputPath.substring(0, extensionIndex) : inputPath;

        return basePath + "_" + resolution + ".mp4";
    }

    private record VariantDefinition(
            String resolution, int height, long bitrate
    ) {
    }
}