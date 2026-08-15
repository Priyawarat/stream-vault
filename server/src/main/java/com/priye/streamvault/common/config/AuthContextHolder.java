package com.priye.streamvault.common.config;

import com.priye.streamvault.user.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthContextHolder {
    public static User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
