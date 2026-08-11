package com.priye.streamvault.video.service.impl;

import com.priye.streamvault.video.service.FFmpegService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FFmpegServiceImpl implements FFmpegService {


    @Override
    public void process(String inputPath, String outputPath) {

        log.info("Starting FFmpeg processing. input={}, output={}", inputPath, outputPath);

//        throw new RuntimeException("TEST-3: Intentional FFmpeg failure for DLQ testing");

        try{

            Process process = getProcess(inputPath, outputPath);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
            }

            log.info("FFmpeg processing completed successfully. output={}", outputPath);

        } catch(Exception e){
            log.error("FFmpeg processing failed. input={}, output={}", inputPath, outputPath, e);
            throw new RuntimeException("Failed to process video using FFmpeg", e);
        }
    }

    /**
     * Creates a Process for executing FFmpeg command with the specified input and output paths.
     * Command: ffmpeg -y -i input.mp4 -c:v libx264 -c:a aac -movflags +faststart output.mp4
     */
    private Process getProcess(String inputPath, String outputPath) throws IOException {

        List<String> command = new ArrayList<>();

        command.add("ffmpeg");

        // Overwrite output if it already exists
        command.add("-y");

        // Input
        command.add("-i");
        command.add(inputPath);

        // Video codec
        command.add("-c:v");
        command.add("libx264");

        // Audio codec
        command.add("-c:a");
        command.add("aac");

        // Streaming-friendly MP4
        command.add("-movflags");
        command.add("+faststart");

        // Output
        command.add(outputPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // FFmpeg writes most information to stderr
        processBuilder.redirectErrorStream(true);

        return processBuilder.start();
    }
}
