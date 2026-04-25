package com.example.orderstudy.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity.status(errorCode.status()).body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidationException() {
        return ResponseEntity.badRequest().body(ErrorResponse.from(ErrorCode.INVALID_REQUEST));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> handleDataIntegrityViolation() {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.status()).body(ErrorResponse.from(ErrorCode.INVALID_REQUEST));
    }
}
