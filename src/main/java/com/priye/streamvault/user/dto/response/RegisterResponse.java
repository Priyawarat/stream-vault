package com.priye.streamvault.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private String message;
    private UUID userId;
}
