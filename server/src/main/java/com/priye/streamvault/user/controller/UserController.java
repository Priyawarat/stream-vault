package com.priye.streamvault.user.controller;

import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.user.dto.request.AuthRequest;
import com.priye.streamvault.user.dto.request.RegisterRequest;
import com.priye.streamvault.user.dto.response.AccessTokenResponse;
import com.priye.streamvault.user.dto.response.AuthResponse;
import com.priye.streamvault.user.dto.response.RegisterResponse;
import com.priye.streamvault.user.service.RegistrationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = registrationService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {

        AuthResponse auth = registrationService.authenticate(request);
        Cookie cookie = new Cookie("refreshToken", auth.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // locally testing purpose
        cookie.setPath("/v1/users");

        response.addCookie(cookie);

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(auth.getAccessToken())
                .build();

        return ResponseEntity.ok(accessTokenResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponse> refreshToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new ResourceNotFoundException("Refresh token not found inside the Cookies");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refresh token not found inside the Cookies"));

        AuthResponse auth = registrationService.refreshToken(refreshToken);

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(auth.getAccessToken())
                .build();

        return ResponseEntity.ok(accessTokenResponse);
    }
}
