package com.priye.streamvault.processing.ffprobe;

import com.priye.streamvault.video.dto.response.FFprobeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FFprobeServiceImpl implements FFprobeService {

    private final ObjectMapper objectMapper;

    public FFprobeServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public FFprobeResult probe(String videoPath) {

        try {

            Process process = getProcess(videoPath);

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFprobe failed with exit code: " + exitCode);
            }

            return parseOutput(output.toString());

        } catch (Exception e) {

            log.error("FFprobe failed for video: {}", videoPath, e);

            throw new RuntimeException("Failed to analyze video using FFprobe", e);
        }
    }

    private static Process getProcess(String videoPath) throws IOException {

        List<String> command = new ArrayList<>();

        command.add("ffprobe");
        command.add("-v");
        command.add("error");

        command.add("-show_entries");
        command.add("format=duration:stream=codec_type,codec_name,width,height");

        command.add("-of");
        command.add("json");

        command.add(videoPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        return processBuilder.start();
    }

    private FFprobeResult parseOutput(String output) throws Exception {

        JsonNode root = objectMapper.readTree(output);

        //Format information
        double duration = 0;

        JsonNode formatNode = root.path("format");

        if (formatNode.has("duration")) {
            duration = formatNode.get("duration").asDouble();
        }

        // Stream information
        int width = 0;
        int height = 0;

        String videoCodec = null;
        String audioCodec = null;

        JsonNode streamsNode = root.path("streams");

        if (streamsNode.isArray()) {

            for (JsonNode stream : streamsNode) {

                String codecType = stream.path("codec_type").asText();

                String codecName = stream.path("codec_name").asText(null);

                if ("video".equals(codecType)) {

                    videoCodec = codecName;

                    width = stream.path("width").asInt();
                    height = stream.path("height").asInt();

                } else if ("audio".equals(codecType)) {
                    audioCodec = codecName;
                }
            }
        }

        return new FFprobeResult(
                duration,
                width,
                height,
                videoCodec,
                audioCodec
        );
    }
}