package com.priye.streamvault.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AuthRequest {
    @Email(message = "Email is not valid")
    private  String email;

    @NotEmpty(message = "Password is required")
    private String password;
}
