package com.finances.dashboard.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.finances.dashboard.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception e) {
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                500,
                "Internal Server Error",
                e.getMessage(),
                LocalDateTime.now()));
    }

}
