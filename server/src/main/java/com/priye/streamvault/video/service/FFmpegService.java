package com.priye.streamvault.video.service;

public interface FFmpegService {

    void process(String inputPath, String outputPath);

    void generateVariant(String inputPath, String outputPath, int height, long bitrate);

}