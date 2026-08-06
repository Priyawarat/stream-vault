package com.priye.streamvault.user.service.impl;

import com.priye.streamvault.common.exception.DuplicateResourceException;
import com.priye.streamvault.user.dto.request.RegisterRequest;
import com.priye.streamvault.user.dto.response.RegisterResponse;
import com.priye.streamvault.user.entity.User;
import com.priye.streamvault.user.repository.UserRepository;
import com.priye.streamvault.user.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail) || userRepository.existsByMobile(request.getMobile())) {
            throw new DuplicateResourceException("User is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("New user registered: userId={}", savedUser.getId());

        return new RegisterResponse("User registered successfully", savedUser.getId());
    }
}
