package com.priye.streamvault.video.service;

import com.priye.streamvault.video.dto.response.FFprobeResult;

public interface FFprobeService {

    FFprobeResult probe(String videoPath);
}