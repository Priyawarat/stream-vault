package com.priye.streamvault.advice;

import com.priye.streamvault.common.exception.ApiError;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private T data;
    private ApiError error;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(ApiError error) {
        this();
        this.error = error;
    }

    public ApiResponse(T data) {
        this();
        this.data = data;
    }
}
