package com.example.ThangLongUniversityWeb.exception;

public abstract class AppException extends RuntimeException {
    protected AppException(String message) {
        super(message);
    }
}

