package com.priye.streamvault.user.controller;

import com.priye.streamvault.user.dto.request.RegisterRequest;
import com.priye.streamvault.user.dto.response.RegisterResponse;
import com.priye.streamvault.user.service.RegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
