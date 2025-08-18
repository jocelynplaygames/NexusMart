package com.example.NexusMart.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;

public class CustomInvalidDataAccessApiUsageException extends InvalidDataAccessApiUsageException {

    public CustomInvalidDataAccessApiUsageException(String message) {
        super(message);
    }

    public CustomInvalidDataAccessApiUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}