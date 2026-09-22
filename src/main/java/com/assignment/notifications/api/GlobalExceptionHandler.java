package com.assignment.notifications.api;

import com.assignment.notifications.api.Dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * One place for error shapes so every endpoint returns the same
 * {type, title, status, detail} envelope instead of default Spring error
 * pages or one-off bodies per controller method.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotificationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "notification-not-found", ex.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IdempotencyConflictException ex) {
        return build(HttpStatus.CONFLICT, "idempotency-key-conflict", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "validation-error", detail);
    }

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    ex.printStackTrace();
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "An unexpected error occurred");
}

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String type, String detail) {
        ErrorResponse body = new ErrorResponse(
                "https://errors.notification-service/" + type,
                status.getReasonPhrase(),
                status.value(),
                detail
        );
        return ResponseEntity.status(status).body(body);
    }
}
