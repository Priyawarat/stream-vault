package com.priye.streamvault.auth.service;

import com.priye.streamvault.auth.dto.request.AuthRequest;
import com.priye.streamvault.auth.dto.request.RegisterRequest;
import com.priye.streamvault.auth.dto.response.AuthResponse;
import com.priye.streamvault.auth.dto.response.RegisterResponse;

public interface RegistrationService {

    RegisterResponse registerUser(RegisterRequest request);
    AuthResponse authenticate(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);

}
