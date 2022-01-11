package com.teamproj.backend.exception;

import com.teamproj.backend.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {

//    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ResponseDto<RestApiException>> handleApiRequestException(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ResponseDto.<RestApiException>builder()
                        .status(HttpStatus.BAD_REQUEST.toString())
                        .message(ex.getMessage())
                        .build());
    }
}
