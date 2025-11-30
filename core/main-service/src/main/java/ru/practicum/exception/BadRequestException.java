package ru.practicum.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException {

    private final String reason;

    public BadRequestException(String message) {
        super(message);
        this.reason = "Incorrectly made request.";
    }

    public BadRequestException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
