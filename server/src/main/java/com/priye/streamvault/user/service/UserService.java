package com.priye.streamvault.user.service;

import com.priye.streamvault.user.entity.User;

import java.util.UUID;

public interface UserService  {
    User getUserById(UUID id);
}
