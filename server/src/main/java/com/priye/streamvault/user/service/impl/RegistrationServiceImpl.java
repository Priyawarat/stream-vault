package com.priye.streamvault.user.service.impl;

import com.priye.streamvault.common.exception.BadRequestException;
import com.priye.streamvault.common.exception.DuplicateResourceException;
import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.user.dto.request.AuthRequest;
import com.priye.streamvault.user.dto.request.RegisterRequest;
import com.priye.streamvault.user.dto.response.AuthResponse;
import com.priye.streamvault.user.dto.response.RegisterResponse;
import com.priye.streamvault.user.entity.User;
import com.priye.streamvault.user.repository.UserRepository;
import com.priye.streamvault.user.service.JwtService;
import com.priye.streamvault.user.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedMobile = request.getMobile().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        if (userRepository.existsByMobile(normalizedMobile)) {
            throw new DuplicateResourceException("Mobile is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .mobile(normalizedMobile)
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("New user registered: userId={}", savedUser.getId());

        return  RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .mobile(savedUser.getMobile())
                .active(savedUser.getActive())
                .build();
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("User account is inactive");
        }

        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            throw new BadRequestException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
     UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
     User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", userId));

     if (!Boolean.TRUE.equals(user.getActive())) {
         throw new BadRequestException("User account is inactive");
     }

     String token = jwtService.generateToken(user);
     return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }
}
