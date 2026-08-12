package com.priye.streamvault.advice;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T>{
    private LocalDateTime timestamp;
    private T data;
}
