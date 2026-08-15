package com.priye.streamvault.video.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record VideoUploadRequest(

        @NotNull(message = "Video file is required")
        MultipartFile file

) {
}