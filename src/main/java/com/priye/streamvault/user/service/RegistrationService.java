package com.priye.streamvault.user.service;

import com.priye.streamvault.user.dto.request.AuthRequest;
import com.priye.streamvault.user.dto.request.RegisterRequest;
import com.priye.streamvault.user.dto.response.AuthResponse;
import com.priye.streamvault.user.dto.response.RegisterResponse;

public interface RegistrationService {

    RegisterResponse registerUser(RegisterRequest request);
    AuthResponse authenticate(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);

}
