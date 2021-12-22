package com.teamproj.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<RestApiException> handleApiRequestException(Exception ex) {

        return ResponseEntity.badRequest()
                .body(RestApiException.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .errorMessage(ex.getMessage())
                        .build());
    }
}
