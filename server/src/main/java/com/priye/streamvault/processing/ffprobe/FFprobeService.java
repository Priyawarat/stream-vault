package com.priye.streamvault.processing.ffprobe;

import com.priye.streamvault.video.dto.response.FFprobeResult;

public interface FFprobeService {

    FFprobeResult probe(String videoPath);
}