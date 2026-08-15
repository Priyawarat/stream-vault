package com.priye.streamvault.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String mobile;
    private boolean active;
}
