package com.priye.streamvault.user.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService  {
    User getUserById(UUID id);
}
