package com.priye.streamvault.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}