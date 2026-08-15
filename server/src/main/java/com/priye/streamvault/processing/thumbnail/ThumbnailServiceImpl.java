package com.priye.streamvault.processing.thumbnail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ThumbnailServiceImpl implements ThumbnailService {

    @Override
    public String generate(String videoPath, String thumbnailPath) {

        log.info("Starting thumbnail generation. video={}, thumbnail={}", videoPath, thumbnailPath);

        try {

            Process process = getProcess(videoPath, thumbnailPath);

            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    ))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    log.debug("Thumbnail FFmpeg: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Thumbnail generation failed with exit code: " + exitCode);
            }

            log.info("Thumbnail generated successfully. thumbnail={}", thumbnailPath);

            return thumbnailPath;

        } catch (Exception e) {

            log.error("Thumbnail generation failed. video={}, thumbnail={}", videoPath, thumbnailPath, e);

            throw new RuntimeException("Failed to generate video thumbnail", e);
        }
    }

    private Process getProcess(String videoPath, String thumbnailPath) throws Exception {

        List<String> command = new ArrayList<>();

        command.add("ffmpeg");

        // Overwrite thumbnail if it already exists
        command.add("-y");

        // Input video
        command.add("-i");
        command.add(videoPath);

        /*
         * Take one frame near the beginning of the video.
         *
         * 0.1 seconds is safer than 1 second for very short videos.
         */
        command.add("-ss");
        command.add("0.1");

        // Generate one frame
        command.add("-frames:v");
        command.add("1");

        // Resize while preserving aspect ratio
        command.add("-vf");
        command.add("scale=640:-2");

        // JPEG quality
        command.add("-q:v");
        command.add("2");

        // Output
        command.add(thumbnailPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        return processBuilder.start();
    }
}