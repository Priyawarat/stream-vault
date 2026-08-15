package com.priye.streamvault.user.service.impl;

import com.priye.streamvault.common.exception.ResourceNotFoundException;
import com.priye.streamvault.user.entity.User;
import com.priye.streamvault.user.repository.UserRepository;
import com.priye.streamvault.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(UUID id) {
        return userRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+id));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User not found with email: "+username));
    }
}
