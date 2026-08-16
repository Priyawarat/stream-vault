package com.priye.streamvault.auth.service;

import com.priye.streamvault.auth.entity.User;

import java.util.UUID;

public interface UserService  {
    User getUserById(UUID id);
}
